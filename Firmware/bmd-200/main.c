/* Copyright (c) 2014 Nordic Semiconductor. All Rights Reserved.
 *
 * The information contained herein is property of Nordic Semiconductor ASA.
 * Terms and conditions of usage are described in detail in NORDIC
 * SEMICONDUCTOR STANDARD SOFTWARE LICENSE AGREEMENT.
 *
 * Licensees are granted free, non-transferable use of the information. NO
 * WARRANTY of ANY KIND is provided. This heading must NOT be removed from
 * the file.
 *
 */

/*! \mainpage BMD-200 Eval Board Demo
 * 	\section intro_sec Introduction
 *  <P>
 *	This is documentation for the Rigado BMD-200 Eval board demo program.
 *  It contains drivers and code example that utilize many features of the eval board, and
 *  gives you a good starting point to get started coding yourself.
 *	<P>
 *  The BMD-200 Eval Board Demo documentation contains discriptions to help you understand some basics on how to code for the BMD-200 module, ARM M0 processors, and the Nordic SoftDevice.
 *	This is only a demonstration and example and should be used as such. Derived code should always be tested with your design.
 * 
 * \section features_sec Features
 * <P>
 * This demo project contains implementations of the following features.
 * <UL>
 *	<LI> RGB LED driving with Pulse width modulation </LI>
 *	<LI> Button state monitoring </LI>
 *	<LI> Analog to digital converter readings </LI>
 *	<LI> Control point characteristic </LI>
 *	<LI> I2C accelerometer configuration and interrupt driven data streaming </LI>
 *	<LI> Custom BLE service </LI>
 * </UL>
 *
 * \section ctrl_sec	Controling the Eval Board
 * <P>
 * To control the various functionalities of the BMD-200 Eval board demo program, you can use the Rigado BMD-200 eval board app for iOS.
 * Alternately, you can use Nordic's Master Control Panel app for iOS, Android, and Windows. You could also write your own application.
 * There are Bluetooth control points avalible for easy use of the board's peripherals.
 * \subsection control_pts Control Points
 * <P>
 * The BMD-200 eval board demo application has two major control points. The first is the LED characteristic. To use it, simply write 
 * a three byte hex RGB value to it. The second control point is the Ctrl characteristic. Several one byte commands can be written to it to control peripherals. 
 * These commands are briefly discribed in the Control_Points_Commands module.  
 * 
 * \section notes_sec Notes
 * <P>
 * \subsection gpiote_notes app_GPIOTE
 * <P>
 * Included is an altered version of app_GPIOTE. Use this instead of the Nordic SDK version. In the version 8.0.0 of
 * of the Nordic SDK there is a minor conflict between nrf_GPIOTE and app_GPIOTE. 
 */


/** @file
 * 
 * @brief BMD-200 Eval board demo main file.
 *
 * @details The main.c file contains the code necessary to initialize and start the Bluetooth SoftDevice stack. It also
 * contains handlers for the button service and Control Point service.
 */
#include <stdbool.h>
#include <stdint.h>
#include <string.h>
#include "nordic_common.h"
#include "nrf.h"
#include <nrf_soc.h>
#include "app_error.h"
#include "nrf_gpio.h"
#include "nrf_gpiote.h"
#include "nrf51_bitfields.h"
#include "ble.h"
#include "ble_hci.h"
#include "ble_srv_common.h"
#include "ble_advdata.h"
#include "ble_advertising.h"
#include "ble_conn_params.h"
#include "app_scheduler.h"
#include "softdevice_handler.h"
#include "app_timer_appsh.h"
#include "app_timer.h"
#include "app_gpiote.h"
#include "app_button.h"
#include "pstorage.h"
#include "ble_bmdeval.h"
#include "bsp.h"
#include "ble_gap.h"
#include "ble_dis.h"
#include "boards.h"
#include "pwm_rgb.h"
#include "bmd_adc.h"
#include "bmd_accel.h"
#include "nrf_delay.h"
#include "version.h"

#define IS_SRVC_CHANGED_CHARACT_PRESENT 1                                           /**< Include or not the service_changed characteristic. if not enabled, the server's database cannot be changed for the lifetime of the device*/

/**
 * @defgroup Control_Point_Commands Control Point Commands
 * @{ 
 * @ingroup Control_Point_Commands
 * @brief   Conrol point command codes. Writing these codes to the ctrl_point characteristic will result in the following behavours.
 */
#define DEBUG_RESERVED					0x00										/**< For Debug use only. */
#define	ADC_STREAM_START				0x01    									/**< Begins ADC streaming. Resulting value is written to the ADC characteristic */
#define ADC_STREAM_STOP					0x02										/**< Stops ADC streaming. Resulting value is written to the ADC characteristic */
#define DEACTIVATE_LEDS					0x03	    								/**< Turns off and deinitializes LEDs. */
#define	ACCEL_TRANSIENT_START			0x04										/**< Starts vibration detection interrupt mode on accelerometer. Resulting values are written to the Accel characteristic. Will continue streaming data in this mode until reconfigured or ACCEL_STOP is received. */
#define ACCEL_PULSE_START				0x05										/**< Starts pulse interrupt mode on accelerometer. Resulting values are written to the Accel characteristic. Will continue streaming data in this mode until reconfigured or ACCEL_STOP is received. */
#define ACCEL_STREAM_START				0x06										/**< Starts auto streaming of Acclerometer. Resulting values are written to the Accel characteristic. Will continue streaming data in this mode until reconfigured or ACCEL_STOP is received. */
#define ACCEL_MOTION_START				0x07										/**< Starts motion detect interrupt mode on accelerometer. Resulting values are written to the Accel characteristic. Will continue streaming data in this mode until reconfigured or ACCEL_STOP is received. */
#define ACCEL_ORIENTATION_START			0x08										/**< Starts orientation detect interrupt mode on accelerometer. Resulting values are written to the Accel characteristic. Will continue streaming data in this mode until reconfigured or ACCEL_STOP is received. */
#define ACCEL_STOP						0x09										/**< Stops the Acceleromerter data streaming. */
#define ACCEL_MODE_GET                  0x0A                                        /**< Gets current Accelerometer mode, returns current mode on control point. */
#define SOFT_RESET                      0xE7D6FCA1                                  /**< Soft-Reset command. Resets application and softdevice. The value should be written to the characteristic as A1FCD6E7. */

/** @} */
//Button and LED specific defines
#define LEDBUTTON_RED          		    BSP_LED_0									/**< Red LED. */
#define LEDBUTTON_GREEN            		BSP_LED_1                                   /**< Green LED. */
#define LEDBUTTON_BLUE           		BSP_LED_2                                   /**< Blue LED. */

#define	RGB_RED_MASK					0xFF0000									/**< Mask for red byte of RGB hex triplet. */	
#define	RGB_GREEN_MASK					0x00FF00									/**< Mask for green byte of RGB hex triplet. */
#define RGB_BLUE_MASK					0x0000FF    								/**< Mask for blue byte of RGB hex triplet. */

#define LEDBUTTON_BUTTON_PIN_NO1      	BSP_BUTTON_0								/**< Pin for button 1. */
#define LEDBUTTON_BUTTON_PIN_NO2        BSP_BUTTON_1								/**< Pin for button 2. */
#define BUTTON_1_MASK 					0x01;										/**< Represents location of button 1 flag bit in button value. */
#define BUTTON_2_MASK 					0x10;										/**< Represents location of button 2 flag bit in button value. */
//Device info service specific defines
#define DEVICE_NAME                     "EvalDemo"                    				/**< Name of device. Will be included in the advertising data. */
#define	MANUFACTURER_NAME				"Rigado"									/**< Name of manufaturer name. Required for the device information service. */

//Advertising sepecific defines
#define APP_ADV_INTERVAL                64                                          /**< The advertising interval (in units of 0.625 ms. This value corresponds to 40 ms). */
#define APP_ADV_TIMEOUT_IN_SECONDS      180                                         /**< The advertising timeout (in units of seconds). */
//Timer specific defines
#define APP_TIMER_PRESCALER             0                                           /**< Value of the RTC1 PRESCALER register. */
#define APP_TIMER_MAX_TIMERS            (3 + BSP_APP_TIMERS_NUMBER)                 /**< Maximum number of simultaneously created timers. */
#define APP_TIMER_OP_QUEUE_SIZE         3                                           /**< Size of timer operation queues. */
//PWM specific defines
#define DUTY_CYCLE_SCALE_FACTOR			2.55										/**< The value that you have to divide by to get from 0-255 scale to 0-100 scale. */
//BLE/GAP specific defines
#define MIN_CONN_INTERVAL               MSEC_TO_UNITS(20, UNIT_1_25_MS)             /**< Minimum acceptable connection interval (10 milliseconds). */
#define MAX_CONN_INTERVAL               MSEC_TO_UNITS(50, UNIT_1_25_MS)             /**< Maximum acceptable connection interval (20 milliseconds). */
#define SLAVE_LATENCY                   0                                           /**< Slave latency. */
#define CONN_SUP_TIMEOUT                MSEC_TO_UNITS(4000, UNIT_10_MS)             /**< Connection supervisory timeout (4 seconds). */
#define FIRST_CONN_PARAMS_UPDATE_DELAY  APP_TIMER_TICKS(5000, APP_TIMER_PRESCALER)  /**< Time from initiating event (connect or start of notification) to first time sd_ble_gap_conn_param_update is called (5 seconds). */
#define NEXT_CONN_PARAMS_UPDATE_DELAY   APP_TIMER_TICKS(30000, APP_TIMER_PRESCALER) /**< Time between each call to sd_ble_gap_conn_param_update after the first call (30 seconds). */
#define MAX_CONN_PARAMS_UPDATE_COUNT    3                                           /**< Number of attempts before giving up the connection parameter negotiation. */
//GPIOTE specific defines
#define APP_GPIOTE_MAX_USERS            4                                           /**< Maximum number of users of the GPIOTE handler. (Button and Accelerometer modules). */

#define BUTTON_DETECTION_DELAY          APP_TIMER_TICKS(50, APP_TIMER_PRESCALER)    /**< Delay from a GPIOTE event until a button is reported as pushed (in number of timer ticks). */

#define DATA_POLLING_INTERVAL			APP_TIMER_TICKS(80, APP_TIMER_PRESCALER) 	/**< Interval between data polling events (ADC, Accelerometer) */
//More GAP specific defines
#define SEC_PARAM_BOND                  1                                           /**< Perform bonding. */
#define SEC_PARAM_MITM                  0                                           /**< Man In The Middle protection not required. */
#define SEC_PARAM_IO_CAPABILITIES       BLE_GAP_IO_CAPS_NONE                        /**< No I/O capabilities. */
#define SEC_PARAM_OOB                   0                                           /**< Out Of Band data not available. */
#define SEC_PARAM_MIN_KEY_SIZE          7                                           /**< Minimum encryption key size. */
#define SEC_PARAM_MAX_KEY_SIZE          16                                          /**< Maximum encryption key size. */

#define DEAD_BEEF                       0xBEEFBEEF                                  /**< Value used as error code on stack dump, can be used to identify stack location on stack unwind. */

#define SCHED_MAX_EVENT_DATA_SIZE       sizeof(app_timer_event_t)                   /**< Maximum size of scheduler events. Note that scheduler BLE stack events do not contain any data, as the events are being pulled from the stack in the event handler. */
#define SCHED_QUEUE_SIZE                10                                          /**< Maximum number of events in the scheduler queue. */

static ble_gap_sec_params_t             m_sec_params;                               /**< Security requirements for this application. */
static uint16_t                         m_conn_handle = BLE_CONN_HANDLE_INVALID;    /**< Handle of the current connection. */
static ble_bmdeval_t                    m_bmdeval;									/**< Structure to Identify the LED_Button service. */
static app_timer_id_t					data_poll_timer_id;							/**< Accelerometer app timer indentifier structure. */

//event handlers in file
static void buttons_init(void);
static void button_event_handler(uint8_t pin_transition_t, uint8_t button_action);
static void data_polling_handler(void *);

// Initialize UUIDs for service(s) used in your application.
ble_uuid_t m_adv_uuids[] = { {BMDEVAL_UUID_SERVICE,	BLE_UUID_TYPE_BLE} };         /**< Universally unique service identifiers. */

// Persistent storage system event handler
void pstorage_sys_event_handler (uint32_t p_evt);

/**@brief Callback function for asserts in the SoftDevice.
 *
 * @details This function will be called in case of an assert in the SoftDevice.
 *
 * @warning On assert from the SoftDevice, the system can only recover on reset.
 *
 * @param[in] line_num   Line number of the failing ASSERT call.
 * @param[in] p_file_name  File name of the failing ASSERT call.
 */
void assert_nrf_callback(uint16_t line_num, const uint8_t * p_file_name)
{
    app_error_handler(DEAD_BEEF, line_num, p_file_name);
}


/**@brief Function for the Timer initialization.
 *
 * @details Initializes the timer module.
 */
static void timers_init(void)
{
		// Initialize timer module, making it use the scheduler
    APP_TIMER_APPSH_INIT(APP_TIMER_PRESCALER, APP_TIMER_MAX_TIMERS, APP_TIMER_OP_QUEUE_SIZE, false);
	
		app_timer_create(&data_poll_timer_id, APP_TIMER_MODE_REPEATED, data_polling_handler);
}


/**@brief Function for the GAP initialization.
 *
 * @details This function sets up all the necessary GAP (Generic Access Profile) parameters of the
 *          device including the device name, appearance, and the preferred connection parameters.
 */
static void gap_params_init(void)
{
    uint32_t                err_code;
    ble_gap_conn_params_t   gap_conn_params;
    ble_gap_conn_sec_mode_t sec_mode;

    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&sec_mode);

    err_code = sd_ble_gap_device_name_set(&sec_mode,
                                         (const uint8_t *)DEVICE_NAME,
                                         strlen(DEVICE_NAME));

    APP_ERROR_CHECK(err_code);

   
    err_code = sd_ble_gap_appearance_set(BLE_APPEARANCE_UNKNOWN);
    APP_ERROR_CHECK(err_code);

    memset(&gap_conn_params, 0, sizeof(gap_conn_params));

    gap_conn_params.min_conn_interval = MIN_CONN_INTERVAL;
    gap_conn_params.max_conn_interval = MAX_CONN_INTERVAL;
    gap_conn_params.slave_latency     = SLAVE_LATENCY;
    gap_conn_params.conn_sup_timeout  = CONN_SUP_TIMEOUT;

    err_code = sd_ble_gap_ppcp_set(&gap_conn_params);
    APP_ERROR_CHECK(err_code);
}

/**@brief Function for re-scaling the written hex values to PWM values (percentages).
 *
 * @param[in] p_bmdeval : Pointer to ble_bmdeval service parameters struct.
 * @param[in] rgb_hex : Pointer to RGB data struct.
 */
static void led_write_handler(ble_bmdeval_t * p_bmdeval, ble_bmdeval_rgb_t * rgb_hex)
{	
  rgb_hex->red_value = (uint8_t)(rgb_hex->red_value / DUTY_CYCLE_SCALE_FACTOR);
  rgb_hex->green_value = (uint8_t)(rgb_hex->green_value / DUTY_CYCLE_SCALE_FACTOR);
	rgb_hex->blue_value = (uint8_t)(rgb_hex->blue_value / DUTY_CYCLE_SCALE_FACTOR);
	PWM_RGB_Start(rgb_hex, &m_bmdeval);
}

/**@brief Function for taking values from the Ctrl pt characteristic and executing the correct output functions.
 *
 * @param[in] p_bmdeval : Pointer to ble_bmdeval service parameters struct.
 * @param[in] ctrl_comm : Control point command.
 */
static void ctrl_write_handler(ble_bmdeval_t * p_bmdeval, uint32_t ctrl_comm)
{
	switch(ctrl_comm)
	{
		case DEBUG_RESERVED: //0x00
			//Insert debug code here
			break;
		case ADC_STREAM_START: //0x01
			ADC_Init(&m_bmdeval);
			ADC_Streaming_Mode(true);
			break;
		case ADC_STREAM_STOP: //0x02
			ADC_Streaming_Mode(false);
			ADC_Deinit();
			break;
		case DEACTIVATE_LEDS: //0x03
			PWM_RGB_Stop();
			break;
		case ACCEL_TRANSIENT_START: //0x04
			Accel_Init(&m_bmdeval);
			Accel_Config_Transient();
			Accel_Streaming_Mode(true);
			break;
		case ACCEL_PULSE_START: //0x05
			Accel_Init(&m_bmdeval);
			Accel_Config_Double_Tap();
			Accel_Streaming_Mode(true);
			break;
		case ACCEL_STREAM_START: //0x06
			Accel_Init(&m_bmdeval);
			Accel_Config_Data_Ready();
			Accel_Streaming_Mode(true);
			break;
		case ACCEL_MOTION_START: //0x07
			Accel_Init(&m_bmdeval);
			Accel_Config_Motion();
			Accel_Streaming_Mode(true);
			break;
		case ACCEL_ORIENTATION_START: //0x08
			Accel_Init(&m_bmdeval);
			Accel_Config_Orientation();
			Accel_Streaming_Mode(true);
			break;
		case ACCEL_STOP: //0x09
			Accel_Init(&m_bmdeval);
			Accel_Streaming_Mode(false);
			Accel_Deinit();
			break;
        case ACCEL_MODE_GET: //0x0A
            ble_bmdeval_on_ctrl_change(&m_bmdeval, Accel_Data_Get_Mode());
            break;
        case SOFT_RESET: //0xE7D6FCA1 (write A1FCD6E7 to characteristic)
            sd_ble_gap_disconnect( m_bmdeval.conn_handle, BLE_HCI_REMOTE_USER_TERMINATED_CONNECTION );
            softdevice_handler_sd_disable();
            nrf_delay_us( 500 * 1000 );
            NVIC_SystemReset(); 
            break;
		default:
			//do nothing
			break;
	}
}

/**@brief Function for initializing services that will be used by the application.
 */
static void services_init(void)
{
	uint32_t                err_code;
    ble_dis_init_t          dis_init;
	ble_bmdeval_init_t      init;
	
	
  	//Initializing the device information service
	  memset(&dis_init, 0, sizeof(dis_init));

    ble_srv_ascii_to_utf8(&dis_init.manufact_name_str, (char *)MANUFACTURER_NAME);
    ble_srv_ascii_to_utf8(&dis_init.fw_rev_str, (char *)FIRMWARE_VERSION_STRING);
    
    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&dis_init.dis_attr_md.read_perm);
    BLE_GAP_CONN_SEC_MODE_SET_NO_ACCESS(&dis_init.dis_attr_md.write_perm);

    err_code = ble_dis_init(&dis_init);
	
    APP_ERROR_CHECK(err_code);
	
		//Initialize the BMD Eval service
    memset(&init, 0, sizeof(init));
		init.led_write_handler = led_write_handler;
		init.ctrl_write_handler = ctrl_write_handler;
    err_code = ble_bmdeval_init(&m_bmdeval, &init);
    APP_ERROR_CHECK(err_code);
    
}


/**@brief Function for initializing security parameters.
 */
static void sec_params_init(void)
{
    m_sec_params.bond         = SEC_PARAM_BOND;
    m_sec_params.mitm         = SEC_PARAM_MITM;
    m_sec_params.io_caps      = SEC_PARAM_IO_CAPABILITIES;
    m_sec_params.oob          = SEC_PARAM_OOB;
    m_sec_params.min_key_size = SEC_PARAM_MIN_KEY_SIZE;
    m_sec_params.max_key_size = SEC_PARAM_MAX_KEY_SIZE;
}


/**@brief Function for handling the Connection Parameters Module.
 *
 * @details This function will be called for all events in the Connection Parameters Module which
 *          are passed to the application.
 *          @note All this function does is to disconnect. This could have been done by simply
 *                setting the disconnect_on_fail config parameter, but instead we use the event
 *                handler mechanism to demonstrate its use.
 *
 * @param[in] p_evt  Event received from the Connection Parameters Module.
 */
static void on_conn_params_evt(ble_conn_params_evt_t * p_evt)
{
    uint32_t err_code;

    if(p_evt->evt_type == BLE_CONN_PARAMS_EVT_FAILED)
    {
        err_code = sd_ble_gap_disconnect(m_conn_handle, BLE_HCI_CONN_INTERVAL_UNACCEPTABLE);
        APP_ERROR_CHECK(err_code);
    }
}


/**@brief Function for handling a Connection Parameters error.
 *
 * @param[in] nrf_error  Error code containing information about what went wrong.
 */
static void conn_params_error_handler(uint32_t nrf_error)
{
    APP_ERROR_HANDLER(nrf_error);
}


/**@brief Function for initializing the Connection Parameters module.
 */
static void conn_params_init(void)
{
    uint32_t               err_code;
    ble_conn_params_init_t cp_init;

    memset(&cp_init, 0, sizeof(cp_init));

    cp_init.p_conn_params                  = NULL;
    cp_init.first_conn_params_update_delay = FIRST_CONN_PARAMS_UPDATE_DELAY;
    cp_init.next_conn_params_update_delay  = NEXT_CONN_PARAMS_UPDATE_DELAY;
    cp_init.max_conn_params_update_count   = MAX_CONN_PARAMS_UPDATE_COUNT;
    cp_init.start_on_notify_cccd_handle    = BLE_GATT_HANDLE_INVALID;
    cp_init.disconnect_on_fail             = false;
    cp_init.evt_handler                    = on_conn_params_evt;
    cp_init.error_handler                  = conn_params_error_handler;

    err_code = ble_conn_params_init(&cp_init);
    APP_ERROR_CHECK(err_code);
}


/**@brief Function for starting timers.
*/
static void timers_start(void)
{
		uint32_t err_code;
	
		err_code = app_timer_start(data_poll_timer_id, DATA_POLLING_INTERVAL, NULL);
		APP_ERROR_CHECK(err_code);
}


/**@brief Function for putting the chip into sleep mode.
 *
 * @note This function will not return.
 */
static void sleep_mode_enter(void)
{
    uint32_t err_code = bsp_indication_set(BSP_INDICATE_IDLE);
    APP_ERROR_CHECK(err_code);

    // Go to system-off mode (this function will not return; wakeup will cause a reset).
    err_code = sd_power_system_off();
    APP_ERROR_CHECK(err_code);
}


/**@brief Function for handling advertising events.
 *
 * @details This function will be called for advertising events which are passed to the application.
 *
 * @param[in] ble_adv_evt  Advertising event.
 */
static void on_adv_evt(ble_adv_evt_t ble_adv_evt)
{
    uint32_t err_code;

    switch (ble_adv_evt)
    {
        case BLE_ADV_EVT_FAST:
            err_code = bsp_indication_set(BSP_INDICATE_ADVERTISING);
            APP_ERROR_CHECK(err_code);
            break;

        case BLE_ADV_EVT_IDLE:
            sleep_mode_enter();
            break;

        default:
            break;
    }
}


/**@brief Function for handling the Application's BLE Stack events.
 *
 * @param[in]   p_ble_evt   Bluetooth stack event.
 */
static void on_ble_evt(ble_evt_t * p_ble_evt)
{
    uint32_t                         err_code;
    static ble_gap_evt_auth_status_t m_auth_status;
    bool                             master_id_matches;
    ble_gap_sec_kdist_t *            p_distributed_keys;
    ble_gap_enc_info_t *             p_enc_info;
    ble_gap_irk_t *                  p_id_info;
    ble_gap_sign_info_t *            p_sign_info;

    static ble_gap_enc_key_t         m_enc_key;           /**< Encryption Key (Encryption Info and Master ID). */
    static ble_gap_id_key_t          m_id_key;            /**< Identity Key (IRK and address). */
    static ble_gap_sign_info_t       m_sign_key;          /**< Signing Key (Connection Signature Resolving Key). */
    static ble_gap_sec_keyset_t      m_keys = {.keys_periph = {&m_enc_key, &m_id_key, &m_sign_key}};

    switch (p_ble_evt->header.evt_id)
    {
        case BLE_GAP_EVT_CONNECTED:
            err_code = bsp_indication_set(BSP_INDICATE_CONNECTED);
            APP_ERROR_CHECK(err_code);
            m_conn_handle = p_ble_evt->evt.gap_evt.conn_handle;
			PWM_RGB_Init(&m_bmdeval);
            break;

        case BLE_GAP_EVT_DISCONNECTED:
            m_conn_handle = BLE_CONN_HANDLE_INVALID;


            //Deinitialize all other peripherals.
            //err_code = app_button_disable();
            Accel_Streaming_Mode(false);
            Accel_Deinit();
            PWM_RGB_Stop();
            ADC_Deinit();
			
            err_code = ble_advertising_start(BLE_ADV_MODE_FAST);
            APP_ERROR_CHECK(err_code);
            break;

        case BLE_GAP_EVT_SEC_PARAMS_REQUEST:
            err_code = sd_ble_gap_sec_params_reply(m_conn_handle,
                                                   BLE_GAP_SEC_STATUS_SUCCESS,
                                                   &m_sec_params,
                                                   &m_keys);
            APP_ERROR_CHECK(err_code);
            break;

        case BLE_GATTS_EVT_SYS_ATTR_MISSING:
            err_code = sd_ble_gatts_sys_attr_set(m_conn_handle,
                                                 NULL,
                                                 0,
                                                 BLE_GATTS_SYS_ATTR_FLAG_SYS_SRVCS | BLE_GATTS_SYS_ATTR_FLAG_USR_SRVCS);
            APP_ERROR_CHECK(err_code);
            break;

        case BLE_GAP_EVT_AUTH_STATUS:
            m_auth_status = p_ble_evt->evt.gap_evt.params.auth_status;
            break;

        case BLE_GAP_EVT_SEC_INFO_REQUEST:
            master_id_matches  = memcmp(&p_ble_evt->evt.gap_evt.params.sec_info_request.master_id,
                                        &m_enc_key.master_id,
                                        sizeof(ble_gap_master_id_t)) == 0;
            p_distributed_keys = &m_auth_status.kdist_periph;

            p_enc_info  = (p_distributed_keys->enc  && master_id_matches) ? &m_enc_key.enc_info : NULL;
            p_id_info   = (p_distributed_keys->id   && master_id_matches) ? &m_id_key.id_info   : NULL;
            p_sign_info = (p_distributed_keys->sign && master_id_matches) ? &m_sign_key         : NULL;

            err_code = sd_ble_gap_sec_info_reply(m_conn_handle, p_enc_info, p_id_info, p_sign_info);
                APP_ERROR_CHECK(err_code);
            break;

        default:
            // No implementation needed.
            break;
    }
}


/**@brief Function for dispatching a BLE stack event to all modules with a BLE stack event handler.
 *
 * @details This function is called from the scheduler in the main loop after a BLE stack
 *          event has been received.
 *
 * @param[in] p_ble_evt  Bluetooth stack event.
 */
static void ble_evt_dispatch(ble_evt_t * p_ble_evt)
{
    on_ble_evt(p_ble_evt);
    ble_conn_params_on_ble_evt(p_ble_evt);
    ble_advertising_on_ble_evt(p_ble_evt);
		ble_bmdeval_on_ble_evt(&m_bmdeval, p_ble_evt);
}

/**@brief Function for dispatching a system event to interested modules.
 *
 * @details This function is called from the System event interrupt handler after a system
 *          event has been received.
 *
 * @param[in] sys_evt  System stack event.
 */
static void sys_evt_dispatch(uint32_t sys_evt)
{
    ble_advertising_on_sys_evt(sys_evt);
}

/**@brief Function for initializing the BLE stack.
 *
 * @details Initializes the SoftDevice and the BLE event interrupt.
 */
static void ble_stack_init(void)
{
    uint32_t err_code;

    // Initialize the SoftDevice handler module.
    SOFTDEVICE_HANDLER_INIT(NRF_CLOCK_LFCLKSRC_XTAL_20_PPM, NULL);

#if defined(S110) || defined(S130)
    // Enable BLE stack 
    ble_enable_params_t ble_enable_params;
    memset(&ble_enable_params, 0, sizeof(ble_enable_params));
#ifdef S130
    ble_enable_params.gatts_enable_params.attr_tab_size   = BLE_GATTS_ATTR_TAB_SIZE_DEFAULT;
#endif
    ble_enable_params.gatts_enable_params.service_changed = IS_SRVC_CHANGED_CHARACT_PRESENT;
    err_code = sd_ble_enable(&ble_enable_params);
    APP_ERROR_CHECK(err_code);
#endif
    
    // Register with the SoftDevice handler module for BLE events.
    err_code = softdevice_ble_evt_handler_set(ble_evt_dispatch);
    APP_ERROR_CHECK(err_code);
    
    // Register with the SoftDevice handler module for BLE events.
    err_code = softdevice_sys_evt_handler_set(sys_evt_dispatch);
    APP_ERROR_CHECK(err_code);
}


/**@brief Function for initializing the Advertising functionality.
 */
static void advertising_init(void)
{
    uint32_t      err_code;
    ble_advdata_t advdata;
    ble_uuid_t    adv_uuid;
    
    // Build advertising data struct to pass into @ref ble_advertising_init.
    memset(&advdata, 0, sizeof(advdata));

    adv_uuid.uuid = BMDEVAL_UUID_SERVICE;
    adv_uuid.type = m_bmdeval.uuid_type;
    
    advdata.name_type               = BLE_ADVDATA_SHORT_NAME;
    advdata.include_appearance      = false;
    advdata.flags                   = BLE_GAP_ADV_FLAGS_LE_ONLY_GENERAL_DISC_MODE;
    advdata.uuids_complete.uuid_cnt = 1;
    advdata.uuids_complete.p_uuids  = &adv_uuid;

    ble_adv_modes_config_t options 	= {0};
    options.ble_adv_fast_enabled  	= BLE_ADV_FAST_ENABLED;
    options.ble_adv_fast_interval 	= APP_ADV_INTERVAL;
    options.ble_adv_fast_timeout  	= APP_ADV_TIMEOUT_IN_SECONDS;

    err_code = ble_advertising_init(&advdata, NULL, &options, on_adv_evt, NULL);
    APP_ERROR_CHECK(err_code);
}


/**@brief Function for the Event Scheduler initialization.
 */
static void scheduler_init(void)
{
    APP_SCHED_INIT(SCHED_MAX_EVENT_DATA_SIZE, SCHED_QUEUE_SIZE);
}


/**@brief Function for initializing the GPIOTE handler module.
 */
static void gpiote_init(void)
{
    APP_GPIOTE_INIT(APP_GPIOTE_MAX_USERS);
}

static void button_event_handler(uint8_t pin_no, uint8_t button_action)
{
    uint32_t err_code;
	static uint8_t send_push = 0x00;
	
    switch (pin_no)
    {
        case LEDBUTTON_BUTTON_PIN_NO1:
            if(button_action == BSP_BUTTON_ACTION_PUSH || button_action == BSP_BUTTON_ACTION_LONG_PUSH)
            {
                send_push |= BUTTON_1_MASK;
            } 
            else
            {
                send_push &= ~BUTTON_1_MASK;
            }
			//send_push = button_1_mask ^ send_push; //XOR to toggle current state of the correct byte.
            err_code = ble_bmdeval_on_button_change(&m_bmdeval, send_push);
				
            if (err_code != NRF_SUCCESS &&
                err_code != BLE_ERROR_INVALID_CONN_HANDLE &&
                err_code != NRF_ERROR_INVALID_STATE)
            {
                APP_ERROR_CHECK(err_code);
            }
            
			break;
        case LEDBUTTON_BUTTON_PIN_NO2:
            if(button_action == BSP_BUTTON_ACTION_PUSH || button_action == BSP_BUTTON_ACTION_LONG_PUSH)
            {
                send_push |= BUTTON_2_MASK;
            } 
            else
            {
                send_push &= ~BUTTON_2_MASK;
            }
            
			//send_push = button_2_mask ^ send_push; //XOR to toggle current state of the correct byte.
            err_code = ble_bmdeval_on_button_change(&m_bmdeval, send_push);
				
            if (err_code != NRF_SUCCESS &&
                err_code != BLE_ERROR_INVALID_CONN_HANDLE &&
                err_code != NRF_ERROR_INVALID_STATE)
            {
                APP_ERROR_CHECK(err_code);
            }
            break;
						
        default:
            APP_ERROR_HANDLER(pin_no);
						
            break;
    }
}

/**@brief Function for initializing the button handler module.
 */
static void buttons_init(void)
{
    // Note: Array must be static because a pointer to it will be saved in the Button handler
    //       module.
    static app_button_cfg_t buttons[] =
    {
        {LEDBUTTON_BUTTON_PIN_NO2, APP_BUTTON_ACTIVE_LOW, NRF_GPIO_PIN_PULLUP, button_event_handler},
        {LEDBUTTON_BUTTON_PIN_NO1, APP_BUTTON_ACTIVE_LOW, NRF_GPIO_PIN_PULLUP, button_event_handler}
    };

    app_button_init(buttons, sizeof(buttons) / sizeof(buttons[0]), BUTTON_DETECTION_DELAY);
}
/** @} */

/**@brief Function handler called when the polling timer expires.
 */
static void data_polling_handler(void * p_context)
{
	Accel_Streaming_Handler(); // Handles data polling for accelerometer.
	ADC_Streaming_Handler();  // Handles data polling for ADC
}

/**@brief Function for the Power manager.
 */
static void power_manage(void)
{
    uint32_t err_code = sd_app_evt_wait();
    APP_ERROR_CHECK(err_code);
}

/**@brief Function for application main entry.
 */
int main(void)
{
    uint32_t err_code;
   
// Initialize
	gpiote_init();

    timers_init();
    buttons_init();
    err_code = app_button_enable();
    APP_ERROR_CHECK(err_code);
    ble_stack_init();
    scheduler_init();
    gap_params_init();
    services_init();
    advertising_init();
    conn_params_init();
    sec_params_init();

// Start execution
    timers_start();
    err_code = ble_advertising_start(BLE_ADV_MODE_FAST);
    APP_ERROR_CHECK(err_code);
// Enter main loop
    for (;;)
    {
        app_sched_execute();
        power_manage();
    }
}

