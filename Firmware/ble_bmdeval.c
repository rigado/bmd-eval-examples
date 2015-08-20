/* Copyright (c) 2013 Nordic Semiconductor. All Rights Reserved.
 *
 * Use of this source code is governed by a BSD-style license that can be
 * found in the license.txt file.
 */

/** @file
 * 
 * @brief ble_bmdEval service module. 
 *
 * @details The ble_bmdEval service contains the BLE characteristics required to drive the features of the BMD-200 Eval Board.
 * <P> The characteristics are as follows.
 * <UL>
 *		<LI> General use control point characteristic (Read, Write, Notify) </LI>
 *  	<LI> Button status indicator characteristic (Read, Notify)</LI>
 *		<LI> RGB LED control characteristic (Read, Write, Notify) </LI>
 *		<LI> Analog-to-digital converter reading characteristic (Read, Notify) </LI>
 *		<LI> Accelerometer reading characteristic (Read, Notify) </LI>
 * </UL>
 */

#include "ble_bmdeval.h"
#include <string.h>
#include "nordic_common.h"
#include "ble_srv_common.h"
#include "app_util.h"


//User description data
char desc_led[] = "LED";			/**< Char string for RGB LED characteristic user discription */
char desc_adc[] = "ADC";			/**< Char string for ADC characteristic user discription */
char desc_button[] = "Button";	    /**< Char string for button characteristic user discription */
char desc_ctrl[] = "Ctrl Pt";		/**< Char string for control point characteristic user discription */
char desc_accel[] = "Accel";		/**< Char string for acclerometer characteristic user discription */

uint8_t init_val_gen = 0;
uint8_t init_val_adc = 0;
uint8_t init_val_ctrl = 0;
uint8_t init_val_button = 0;
accel_data_t init_val_accel = {0, 0, 0};
//Write data is a rgb structure for defining color values
ble_bmdeval_rgb_t write_data = {0,0,0};


/**@brief Function for handling the Connect event.
 *
 * @param[in]   p_bmdeval       BMD Eval Service structure.
 * @param[in]   p_ble_evt   Event received from the BLE stack.
 */
static void on_connect(ble_bmdeval_t * p_bmdeval, ble_evt_t * p_ble_evt)
{
    p_bmdeval->conn_handle = p_ble_evt->evt.gap_evt.conn_handle;
}


/**@brief Function for handling the Disconnect event.
 *
 * @param[in]   p_bmdeval       BMD Eval Service structure.
 * @param[in]   p_ble_evt   Event received from the BLE stack.
 */
static void on_disconnect(ble_bmdeval_t * p_bmdeval, ble_evt_t * p_ble_evt)
{
    UNUSED_PARAMETER(p_ble_evt);
    p_bmdeval->conn_handle = BLE_CONN_HANDLE_INVALID;
}


/**@brief Function for handling the Write event.
 *
 * @param[in]   p_bmdeval       BMD Eval Service structure.
 * @param[in]   p_ble_evt   Event received from the BLE stack.
 */
static void on_write(ble_bmdeval_t * p_bmdeval, ble_evt_t * p_ble_evt)
{
    ble_gatts_evt_write_t * p_evt_write = &p_ble_evt->evt.gatts_evt.params.write;
	
 //For LED write events   
    if ((p_evt_write->handle == p_bmdeval->led_char_handles.value_handle) &&
        (p_evt_write->len == 3) &&
        (p_bmdeval->led_write_handler != NULL))
    {
                write_data.red_value   = p_evt_write->data[0];
				write_data.green_value = p_evt_write->data[1];
				write_data.blue_value  = p_evt_write->data[2];
				p_bmdeval->led_write_handler(p_bmdeval, &write_data);
    }
		
	//For control point write events
    if ((p_evt_write->handle == p_bmdeval->ctrl_char_handles.value_handle) &&
        (p_bmdeval->ctrl_write_handler != NULL))
    {
        if(p_evt_write->len == 4)
        {
            uint32_t ctrl_data = p_evt_write->data[3]<<24 | p_evt_write->data[2]<<16 | p_evt_write->data[1]<<8 | p_evt_write->data[0];
            p_bmdeval->ctrl_write_handler(p_bmdeval, ctrl_data);
        }
        else
        {
            p_bmdeval->ctrl_write_handler(p_bmdeval, p_evt_write->data[0]);
        }
    }
}

//Register your events here.
void ble_bmdeval_on_ble_evt(ble_bmdeval_t * p_bmdeval, ble_evt_t * p_ble_evt)
{
    switch (p_ble_evt->header.evt_id)
    {
        case BLE_GAP_EVT_CONNECTED:
            on_connect(p_bmdeval, p_ble_evt);
            break;
            
        case BLE_GAP_EVT_DISCONNECTED:
            on_disconnect(p_bmdeval, p_ble_evt);
            break;
            
        case BLE_GATTS_EVT_WRITE:
            on_write(p_bmdeval, p_ble_evt);
            break;
            
        default:
            // No implementation needed.
            break;
    }
}


/**@brief Function for adding the LED characteristic.
 *
 */
static uint32_t led_char_add(ble_bmdeval_t * p_bmdeval, const ble_bmdeval_init_t * p_bmdeval_init)
{
    ble_gatts_char_md_t char_md;
    ble_gatts_attr_t    attr_char_value;
    ble_uuid_t          ble_uuid;
    ble_gatts_attr_md_t attr_md;

    memset(&char_md, 0, sizeof(char_md));

	char_md.char_props.read           = 1;
    char_md.char_props.write_wo_resp  = 1;
    char_md.p_char_user_desc  			= (uint8_t *) desc_led;
	char_md.char_user_desc_size 		= strlen(desc_led);
	char_md.char_user_desc_max_size     = strlen(desc_led);
    char_md.p_char_pf         = NULL;
    char_md.p_user_desc_md    = NULL;
    char_md.p_cccd_md         = NULL;
    char_md.p_sccd_md         = NULL;
    
    ble_uuid.type = p_bmdeval->uuid_type;
    ble_uuid.uuid = BMDEVAL_UUID_LED_CHAR;
    
    memset(&attr_md, 0, sizeof(attr_md));

    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&attr_md.read_perm);
    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&attr_md.write_perm);
    attr_md.vloc       = BLE_GATTS_VLOC_STACK;
    attr_md.rd_auth    = 0;
    attr_md.wr_auth    = 0;
    attr_md.vlen       = 0;
    
    memset(&attr_char_value, 0, sizeof(attr_char_value));

    attr_char_value.p_uuid       = &ble_uuid;
    attr_char_value.p_attr_md    = &attr_md;
    attr_char_value.init_len     = sizeof(ble_bmdeval_rgb_t); //3 byte length to contain a standard RGB reperesentation. 
    attr_char_value.init_offs    = 0;
    attr_char_value.max_len      = sizeof(ble_bmdeval_rgb_t); //3 byte length to contain a standard RGB reperesentation.
    attr_char_value.p_value      = &init_val_gen;
    
    return sd_ble_gatts_characteristic_add(p_bmdeval->service_handle, &char_md,
                                               &attr_char_value,
                                               &p_bmdeval->led_char_handles);
}

/**@brief Function for adding the ADC characteristic.
 *
 */
static uint32_t adc_char_add(ble_bmdeval_t * p_bmdeval, const ble_bmdeval_init_t * p_bmdeval_init)
{
    ble_gatts_char_md_t char_md;
    ble_gatts_attr_md_t cccd_md;
    ble_gatts_attr_t    attr_char_value;
    ble_uuid_t          ble_uuid;
    ble_gatts_attr_md_t attr_md;

    memset(&cccd_md, 0, sizeof(cccd_md));

    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&cccd_md.read_perm);
    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&cccd_md.write_perm);
    cccd_md.vloc = BLE_GATTS_VLOC_STACK;
    
    memset(&char_md, 0, sizeof(char_md));
    
    char_md.char_props.read   = 1;
    char_md.char_props.notify = 1;
    char_md.p_char_user_desc  			= (uint8_t *) desc_adc;
	char_md.char_user_desc_size 		= strlen(desc_adc);
	char_md.char_user_desc_max_size     = strlen(desc_adc);
    char_md.p_char_pf         = NULL;
    char_md.p_user_desc_md    = NULL;
    char_md.p_cccd_md         = &cccd_md;
    char_md.p_sccd_md         = NULL;
    
    ble_uuid.type = p_bmdeval->uuid_type;
    ble_uuid.uuid = BMDEVAL_UUID_ADC_CHAR;
    
    memset(&attr_md, 0, sizeof(attr_md));

    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&attr_md.read_perm);
    BLE_GAP_CONN_SEC_MODE_SET_NO_ACCESS(&attr_md.write_perm);
    attr_md.vloc       = BLE_GATTS_VLOC_STACK;
    attr_md.rd_auth    = 0;
    attr_md.wr_auth    = 0;
    attr_md.vlen       = 0;
    
    memset(&attr_char_value, 0, sizeof(attr_char_value));

    attr_char_value.p_uuid       = &ble_uuid;
    attr_char_value.p_attr_md    = &attr_md;
    attr_char_value.init_len     = sizeof(uint8_t);
    attr_char_value.init_offs    = 0;
    attr_char_value.max_len      = sizeof(uint8_t);
    attr_char_value.p_value      = &init_val_adc;
    
    return sd_ble_gatts_characteristic_add(p_bmdeval->service_handle, &char_md,
                                               &attr_char_value,
                                               &p_bmdeval->adc_char_handles);
}

/**@brief Function for adding the accelerometer characteristic.
 *
 */
static uint32_t accel_char_add(ble_bmdeval_t * p_bmdeval, const ble_bmdeval_init_t * p_bmdeval_init)
{
    ble_gatts_char_md_t char_md;
    ble_gatts_attr_md_t cccd_md;
    ble_gatts_attr_t    attr_char_value;
    ble_uuid_t          ble_uuid;
    ble_gatts_attr_md_t attr_md;

    memset(&cccd_md, 0, sizeof(cccd_md));

    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&cccd_md.read_perm);
    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&cccd_md.write_perm);
    cccd_md.vloc = BLE_GATTS_VLOC_STACK;
    
    memset(&char_md, 0, sizeof(char_md));
    
    char_md.char_props.read   = 1;
    char_md.char_props.notify = 1;
    char_md.p_char_user_desc  			= (uint8_t *) desc_accel;
	char_md.char_user_desc_size 		= strlen(desc_accel);
	char_md.char_user_desc_max_size     = strlen(desc_accel);
    char_md.p_char_pf         = NULL;
    char_md.p_user_desc_md    = NULL;
    char_md.p_cccd_md         = &cccd_md;
    char_md.p_sccd_md         = NULL;
    
    ble_uuid.type = p_bmdeval->uuid_type;
    ble_uuid.uuid = BMDEVAL_UUID_ACCEL_CHAR;
    
    memset(&attr_md, 0, sizeof(attr_md));

    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&attr_md.read_perm);
    BLE_GAP_CONN_SEC_MODE_SET_NO_ACCESS(&attr_md.write_perm);
    attr_md.vloc       = BLE_GATTS_VLOC_STACK;
    attr_md.rd_auth    = 0;
    attr_md.wr_auth    = 0;
    attr_md.vlen       = 0;
    
    memset(&attr_char_value, 0, sizeof(attr_char_value));

    attr_char_value.p_uuid       = &ble_uuid;
    attr_char_value.p_attr_md    = &attr_md;
    attr_char_value.init_len     = sizeof(accel_data_t);
    attr_char_value.init_offs    = 0;
    attr_char_value.max_len      = sizeof(accel_data_t);
    attr_char_value.p_value      = &init_val_gen;
    
    return sd_ble_gatts_characteristic_add(p_bmdeval->service_handle, &char_md,
                                               &attr_char_value,
                                               &p_bmdeval->accel_char_handles);
}


/**@brief Function for adding the control point characteristic.
 *
 */
static uint32_t ctrl_char_add(ble_bmdeval_t * p_bmdeval, const ble_bmdeval_init_t * p_bmdeval_init)
{
    ble_gatts_char_md_t char_md;
    ble_gatts_attr_t    attr_char_value;
    ble_uuid_t          ble_uuid;
    ble_gatts_attr_md_t attr_md;

    memset(&char_md, 0, sizeof(char_md));
    
    char_md.char_props.read   = 1;
    char_md.char_props.write  = 1;
    char_md.char_props.notify = 1;
    char_md.char_props.write_wo_resp = 1;
    char_md.p_char_user_desc  			= (uint8_t *) desc_ctrl;
		char_md.char_user_desc_size 		= strlen(desc_ctrl);
		char_md.char_user_desc_max_size = strlen(desc_ctrl);
    char_md.p_char_pf         = NULL;
    char_md.p_user_desc_md    = NULL;
    char_md.p_cccd_md         = NULL;
    char_md.p_sccd_md         = NULL;
    
    ble_uuid.type = p_bmdeval->uuid_type;
    ble_uuid.uuid = BMDEVAL_UUID_CTRL_CHAR;
    
    memset(&attr_md, 0, sizeof(attr_md));

    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&attr_md.read_perm);
    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&attr_md.write_perm);
    attr_md.vloc       = BLE_GATTS_VLOC_STACK;
    attr_md.rd_auth    = 0;
    attr_md.wr_auth    = 0;
    attr_md.vlen       = 1;
    
    memset(&attr_char_value, 0, sizeof(attr_char_value));

    attr_char_value.p_uuid       = &ble_uuid;
    attr_char_value.p_attr_md    = &attr_md;
    attr_char_value.init_len     = sizeof(uint8_t); //1 byte length to contain a control point command. 
    attr_char_value.init_offs    = 0;
    attr_char_value.max_len      = 20; //Enough space for a longer reset command.
    attr_char_value.p_value      = &init_val_ctrl;
    
    return sd_ble_gatts_characteristic_add(p_bmdeval->service_handle, &char_md,
                                               &attr_char_value,
                                               &p_bmdeval->ctrl_char_handles);
}

/**@brief Function for adding the Button characteristic.
 *
 */
static uint32_t button_char_add(ble_bmdeval_t * p_bmdeval, const ble_bmdeval_init_t * p_bmdeval_init)
{
    ble_gatts_char_md_t char_md;
    ble_gatts_attr_md_t cccd_md;
    ble_gatts_attr_t    attr_char_value;
    ble_uuid_t          ble_uuid;
    ble_gatts_attr_md_t attr_md;

    memset(&cccd_md, 0, sizeof(cccd_md));

    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&cccd_md.read_perm);
    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&cccd_md.write_perm);
    cccd_md.vloc = BLE_GATTS_VLOC_STACK;
    
    memset(&char_md, 0, sizeof(char_md));
    
    char_md.char_props.read   = 1;
    char_md.char_props.notify = 1;
    char_md.p_char_user_desc  			= (uint8_t *) desc_button;
	char_md.char_user_desc_size 		= strlen(desc_button);
	char_md.char_user_desc_max_size     = strlen(desc_button);
    char_md.p_char_pf         = NULL;
    char_md.p_user_desc_md    = NULL;
    char_md.p_cccd_md         = &cccd_md;
    char_md.p_sccd_md         = NULL;
    
    ble_uuid.type = p_bmdeval->uuid_type;
    ble_uuid.uuid = BMDEVAL_UUID_BUTTON_CHAR;
    
    memset(&attr_md, 0, sizeof(attr_md));

    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&attr_md.read_perm);
    BLE_GAP_CONN_SEC_MODE_SET_NO_ACCESS(&attr_md.write_perm);
    attr_md.vloc       = BLE_GATTS_VLOC_STACK;
    attr_md.rd_auth    = 0;
    attr_md.wr_auth    = 0;
    attr_md.vlen       = 0;
    
    memset(&attr_char_value, 0, sizeof(attr_char_value));

    attr_char_value.p_uuid       = &ble_uuid;
    attr_char_value.p_attr_md    = &attr_md;
    attr_char_value.init_len     = sizeof(uint8_t);
    attr_char_value.init_offs    = 0;
    attr_char_value.max_len      = sizeof(uint8_t);
    attr_char_value.p_value      = &init_val_button;
    
    return sd_ble_gatts_characteristic_add(p_bmdeval->service_handle, &char_md,
                                               &attr_char_value,
                                               &p_bmdeval->button_char_handles);
}

uint32_t ble_bmdeval_init(ble_bmdeval_t * p_bmdeval, const ble_bmdeval_init_t * p_bmdeval_init)
{
    uint32_t   err_code;
    ble_uuid_t ble_uuid;

    // Initialize service structure.
    p_bmdeval->conn_handle       = BLE_CONN_HANDLE_INVALID;
    p_bmdeval->led_write_handler = p_bmdeval_init->led_write_handler;
		p_bmdeval->ctrl_write_handler = p_bmdeval_init->ctrl_write_handler;
		
    // Add service
    ble_uuid128_t base_uuid = {BMDEVAL_UUID_BASE};
    err_code = sd_ble_uuid_vs_add(&base_uuid, &p_bmdeval->uuid_type);
    if (err_code != NRF_SUCCESS)
    {
        return err_code;
    }
    
    ble_uuid.type = p_bmdeval->uuid_type;
    ble_uuid.uuid = BMDEVAL_UUID_SERVICE;

    err_code = sd_ble_gatts_service_add(BLE_GATTS_SRVC_TYPE_PRIMARY, &ble_uuid, &p_bmdeval->service_handle);
    if (err_code != NRF_SUCCESS)
    {
        return err_code;
    }
    
    err_code = button_char_add(p_bmdeval, p_bmdeval_init);
    if (err_code != NRF_SUCCESS)
    {
        return err_code;
    }
    
    err_code = led_char_add(p_bmdeval, p_bmdeval_init);
    if (err_code != NRF_SUCCESS)
    {
        return err_code;
    }
		
		err_code = ctrl_char_add(p_bmdeval, p_bmdeval_init);
    if (err_code != NRF_SUCCESS)
    {
        return err_code;
    }
		
		err_code = adc_char_add(p_bmdeval, p_bmdeval_init);
    if (err_code != NRF_SUCCESS)
    {
        return err_code;
    }
		
		err_code = accel_char_add(p_bmdeval, p_bmdeval_init);
    if (err_code != NRF_SUCCESS)
    {
        return err_code;
    }		
		
    
    return NRF_SUCCESS;
}

uint32_t ble_bmdeval_on_button_change(ble_bmdeval_t * p_bmdeval, uint8_t button_state)
{
    if(p_bmdeval->conn_handle == BLE_CONN_HANDLE_INVALID)
    {
        return BLE_ERROR_INVALID_CONN_HANDLE;
    }
    ble_gatts_hvx_params_t params;
    uint16_t len = sizeof(button_state);
    
    memset(&params, 0, sizeof(params));
    
		params.handle = p_bmdeval->button_char_handles.value_handle;
		params.type = BLE_GATT_HVX_NOTIFICATION;
    //params.offset = 0;
		params.p_data = &button_state;
    params.p_len = &len;
		
    return sd_ble_gatts_hvx(p_bmdeval->conn_handle, &params);
}

uint32_t ble_bmdeval_on_adc_change(ble_bmdeval_t * p_bmdeval, uint8_t adc_value)
{
    ble_gatts_hvx_params_t params;
    uint16_t len = sizeof(adc_value);
    
    memset(&params, 0, sizeof(params));
    
		params.handle = p_bmdeval->adc_char_handles.value_handle;
		params.type = BLE_GATT_HVX_NOTIFICATION;
    //params.offset = 0;
		params.p_data = &adc_value;
    params.p_len = &len;
		
    return sd_ble_gatts_hvx(p_bmdeval->conn_handle, &params);
}

uint32_t ble_bmdeval_on_accel_change(ble_bmdeval_t * p_bmdeval,  accel_data_t accel_value)
{
	ble_gatts_hvx_params_t params;
    uint16_t len = sizeof(accel_data_t);
    
    memset(&params, 0, sizeof(params));
    
	params.handle = p_bmdeval->accel_char_handles.value_handle;
	params.type = BLE_GATT_HVX_NOTIFICATION;
    //params.offset = 0;
	params.p_data = (uint8_t *)&accel_value;
	params.p_len = &len;
		
    return sd_ble_gatts_hvx(p_bmdeval->conn_handle, &params);
}

uint32_t ble_bmdeval_on_ctrl_change(ble_bmdeval_t * p_bmdeval,  uint8_t ctrl_value)
{
	ble_gatts_hvx_params_t params;
    uint16_t len = sizeof(uint8_t);
    
    memset(&params, 0, sizeof(params));
    
	params.handle = p_bmdeval->ctrl_char_handles.value_handle;
	params.type = BLE_GATT_HVX_NOTIFICATION;
    //params.offset = 0;
	params.p_data =&ctrl_value;
	params.p_len = &len;
		
    return sd_ble_gatts_hvx(p_bmdeval->conn_handle, &params);
}

uint32_t ble_bmdeval_on_pwm_change(ble_bmdeval_t * p_bmdeval, ble_bmdeval_rgb_t pwm_state)
{
    if(p_bmdeval->conn_handle == BLE_CONN_HANDLE_INVALID)
    {
        return BLE_ERROR_INVALID_CONN_HANDLE;
    }
    ble_gatts_hvx_params_t params;
    uint16_t len = sizeof(ble_bmdeval_rgb_t);
    
    memset(&params, 0, sizeof(params));
    
		params.handle = p_bmdeval->led_char_handles.value_handle;
		params.type = BLE_GATT_HVX_NOTIFICATION;
    //params.offset = 0;
		params.p_data = (uint8_t *)&pwm_state;
        params.p_len = &len;
		
    return sd_ble_gatts_hvx(p_bmdeval->conn_handle, &params);
}
