/* Copyright (c) 2013 Nordic Semiconductor. All Rights Reserved.
 *
 * Use of this source code is governed by a BSD-style license that can be
 * found in the license.txt file.
 */

/** @file ble_bmdeval.c
 * 
 * @brief ble_bmdeval service module. 
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
 *
 * @par
 * COPYRIGHT NOTICE: (c) Rigado
 * All rights reserved. 
 *
 * Source code licensed under Software License Agreement in license.txt.
 * You should have received a copy with purchase of BMD series product 
 * and with this repository.  If not, contact modules@rigado.com.
 */
#include <string.h>
#include "nordic_common.h"
#include "ble_srv_common.h"
#include "app_util.h"

#include "bmd_accel.h"
#include "bmd_adc.h"
#include "bmd_pwm_rgb_led.h"

#include "ble_bmdeval.h"

//User description data
static const char * desc_led = "LED";			/**< Char string for RGB LED characteristic user discription */
static const char * desc_adc = "ADC";			/**< Char string for ADC characteristic user discription */
static const char * desc_button = "Button";	    /**< Char string for button characteristic user discription */
static const char * desc_ctrl = "Ctrl Pt";		/**< Char string for control point characteristic user discription */
static const char * desc_accel = "Accel";		/**< Char string for acclerometer characteristic user discription */

static uint8_t init_val_gen = 0;
static uint8_t init_val_adc = 0;
static uint8_t init_val_ctrl = 0;
static uint8_t init_val_button = 0;

#define VALID_CONNECTION_HANDLE(handle)     (BLE_CONN_HANDLE_INVALID != handle)

static void build_notification(ble_gatts_hvx_params_t * const p_params, 
                            uint16_t handle,
                            uint8_t type,
                            uint16_t offset,
                            uint16_t * p_length,
                            uint8_t * p_data);

/**@brief Function for handling the Connect event.
 *
 * @param[in]   p_bmdeval       BMD Eval Service structure.
 * @param[in]   p_ble_evt   Event received from the BLE stack.
 */
static void on_connect(ble_bmdeval_t * p_bmdeval, ble_evt_t * p_ble_evt)
{
    p_bmdeval->conn_handle = p_ble_evt->evt.gap_evt.conn_handle;
    bmd_pwm_rgb_color_t color = { 0xFF, 0xFF, 0xFF };
    bmd_pwm_rgb_led_start(&color);
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
    bmd_accel_enable_interrupt(false, NULL);
    bmd_accel_enable(false);
    bmd_adc_set_streaming_state(false);
    bmd_pwm_rgb_led_stop();
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
        bmd_pwm_rgb_color_t color;
        color.red = p_evt_write->data[0];
        color.green = p_evt_write->data[1];
        color.blue = p_evt_write->data[2];
        p_bmdeval->led_write_handler(p_bmdeval, &color);
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

/** @brief Handler for ble events
 *
 */
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

	char_md.char_props.read             = 1;
    char_md.char_props.write_wo_resp    = 1;
    char_md.char_props.write            = 1;
    char_md.p_char_user_desc  			= (uint8_t *) desc_led;
	char_md.char_user_desc_size 		= strlen(desc_led);
	char_md.char_user_desc_max_size     = strlen(desc_led);
    char_md.p_char_pf           = NULL;
    char_md.p_user_desc_md      = NULL;
    char_md.p_cccd_md           = NULL;
    char_md.p_sccd_md         =  NULL;
    
    ble_uuid.type = p_bmdeval->uuid_type;
    ble_uuid.uuid = BMDEVAL_UUID_LED_CHAR;
    
    memset(&attr_md, 0, sizeof(attr_md));

    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&attr_md.read_perm);
    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&attr_md.write_perm);
    attr_md.vloc        = BLE_GATTS_VLOC_STACK;
    attr_md.rd_auth     = 0;
    attr_md.wr_auth     = 0;
    attr_md.vlen        = 0;
    
    memset(&attr_char_value, 0, sizeof(attr_char_value));

    attr_char_value.p_uuid      = &ble_uuid;
    attr_char_value.p_attr_md   = &attr_md;
    attr_char_value.init_len    = sizeof(ble_bmdeval_rgb_t);
    attr_char_value.init_offs   = 0;
    attr_char_value.max_len     = sizeof(ble_bmdeval_rgb_t);
    attr_char_value.p_value     = &init_val_gen;
    
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
    attr_char_value.init_len     = sizeof(uint8_t); 
    attr_char_value.init_offs    = 0;
    attr_char_value.max_len      = 20;
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

uint32_t ble_bmdeval_init(ble_bmdeval_t * p_bmdeval, 
                            const ble_bmdeval_init_t * p_bmdeval_init)
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

/** @brief Fuction to handle button state change notification
 *
 */
uint32_t ble_bmdeval_on_button_change(const ble_bmdeval_t * p_bmdeval, uint8_t button_state)
{
    if(!VALID_CONNECTION_HANDLE(p_bmdeval->conn_handle))
    {
        return BLE_ERROR_INVALID_CONN_HANDLE;
    }
    
    ble_gatts_hvx_params_t params;
    uint16_t len = sizeof(button_state);
    
    build_notification(&params, p_bmdeval->button_char_handles.value_handle,
                    BLE_GATT_HVX_NOTIFICATION, 0, &len, 
                    &button_state);
		
    return sd_ble_gatts_hvx(p_bmdeval->conn_handle, &params);
}

/** @brief Function to handle adc update notification
 *
 */
uint32_t ble_bmdeval_on_adc_change(const ble_bmdeval_t * p_bmdeval, uint8_t adc_value)
{
    if(!VALID_CONNECTION_HANDLE(p_bmdeval->conn_handle))
    {
        return BLE_ERROR_INVALID_CONN_HANDLE;
    }
    
    ble_gatts_hvx_params_t params;
    uint16_t len = sizeof(adc_value);
    
    build_notification(&params, p_bmdeval->adc_char_handles.value_handle,
                    BLE_GATT_HVX_NOTIFICATION, 0, &len, 
                    &adc_value);
		
    return sd_ble_gatts_hvx(p_bmdeval->conn_handle, &params);
}

/** @brief Function to handle accelerometer data notification
 *
 */
uint32_t ble_bmdeval_on_accel_change(const ble_bmdeval_t * p_bmdeval, 
                                     const accel_data_t * const p_accel_value)
{
    if(!VALID_CONNECTION_HANDLE(p_bmdeval->conn_handle))
    {
        return BLE_ERROR_INVALID_CONN_HANDLE;
    }
    
	ble_gatts_hvx_params_t params;
    uint16_t len = sizeof(accel_data_t);
    
    build_notification(&params, p_bmdeval->accel_char_handles.value_handle,
                    BLE_GATT_HVX_NOTIFICATION, 0, &len, 
                    (uint8_t *)p_accel_value);

    return sd_ble_gatts_hvx(p_bmdeval->conn_handle, &params);
}

/** @brief Function to handle control point notification
 *
 */
uint32_t ble_bmdeval_on_ctrl_change(const ble_bmdeval_t * p_bmdeval,  uint8_t ctrl_value)
{
    if(!VALID_CONNECTION_HANDLE(p_bmdeval->conn_handle))
    {
        return BLE_ERROR_INVALID_CONN_HANDLE;
    }
    
	ble_gatts_hvx_params_t params;
    uint16_t len = sizeof(uint8_t);
    
    build_notification(&params, p_bmdeval->ctrl_char_handles.value_handle,
                        BLE_GATT_HVX_NOTIFICATION, 0, &len, 
                        &ctrl_value);
		
    return sd_ble_gatts_hvx(p_bmdeval->conn_handle, &params);
}

/** @brief Function to handle led data notification
 *
 */
uint32_t ble_bmdeval_on_pwm_change(const ble_bmdeval_t * p_bmdeval, ble_bmdeval_rgb_t pwm_state)
{
    if(!VALID_CONNECTION_HANDLE(p_bmdeval->conn_handle))
    {
        return BLE_ERROR_INVALID_CONN_HANDLE;
    }
    
    ble_gatts_hvx_params_t params;
    uint16_t len = sizeof(ble_bmdeval_rgb_t);
    
    build_notification(&params, p_bmdeval->led_char_handles.value_handle,
                        BLE_GATT_HVX_NOTIFICATION, 0, &len, 
                        (uint8_t *)&pwm_state);
    	
    return sd_ble_gatts_hvx(p_bmdeval->conn_handle, &params);
}

/* Helper function to build a notification packet */
static void build_notification(ble_gatts_hvx_params_t * const p_params, 
                            uint16_t handle,
                            uint8_t type,
                            uint16_t offset,
                            uint16_t * p_length,
                            uint8_t * p_data)
{
    memset(p_params, 0, sizeof(ble_gatts_hvx_params_t));
    
    p_params->handle = handle;
    p_params->type = type;
    p_params->offset = offset;
    p_params->p_len = p_length;
    p_params->p_data = p_data;
}
