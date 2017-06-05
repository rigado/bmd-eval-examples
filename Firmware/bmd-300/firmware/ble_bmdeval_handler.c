/** @file ble_bmdeval_handler.c
*
* @brief This module handles incoming write events from the ble_bmdeval_t
*        service and manages data reads from the accelerometer.
*
* @par
* COPYRIGHT NOTICE: (c) Rigado
* All rights reserved. 
*
* Source code licensed under Software License Agreement in license.txt.
* You should have received a copy with purchase of BMD series product 
* and with this repository.  If not, contact modules@rigado.com.
*/
#include <stdlib.h>

#include "ble_hci.h"
#include "nrf_delay.h"
#include "nrf_error.h"
#include "softdevice_handler.h"

#include "bmd_accel.h"
#include "bmd_adc.h"
#include "bmd_pwm_rgb_led.h"

#include "ble_bmdeval_handler.h"
#include "ble_bmdeval_commands.h"

static ble_bmdeval_t * mp_bmdeval;

/* Helper functions */
static void ctrl_write_handler(ble_bmdeval_t * p_bmdeval, uint32_t ctrl_comm);
static void led_write_handler(ble_bmdeval_t * p_bmdeval, 
                                bmd_pwm_rgb_color_t * rgb_hex);
static void accel_data_handler(void);

uint32_t ble_bmdeval_handler_init(ble_bmdeval_init_t * const p_ble_bmdeval_init,
                                    ble_bmdeval_t * p_bmdeval)
{
    if(p_ble_bmdeval_init == NULL)
    {
        return NRF_ERROR_NULL;
    }
    
    p_ble_bmdeval_init->ctrl_write_handler = ctrl_write_handler;
    p_ble_bmdeval_init->led_write_handler = led_write_handler;
    
    mp_bmdeval = p_bmdeval;
    
    return NRF_SUCCESS;
}

/**@brief Handles updates to the RGB led.
 *
 * @param[in] p_bmdeval : Pointer to ble_bmdeval service parameters struct.
 * @param[in] rgb_hex : Pointer to RGB data struct.
 */
static void led_write_handler(ble_bmdeval_t * p_bmdeval, 
                              bmd_pwm_rgb_color_t * rgb_hex)
{	
    rgb_hex->red = (0xFF - rgb_hex->red);
    rgb_hex->green = (0xFF - rgb_hex->green);
    rgb_hex->blue = (0xFF - rgb_hex->blue);
	bmd_pwm_rgb_led_set_color(rgb_hex);
}

/**@brief Function for taking values from the control point characteristic 
 *        and executing the correct output functions.
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
			bmd_adc_set_streaming_state(true);
			break;
		case ADC_STREAM_STOP: //0x02
			bmd_adc_set_streaming_state(false);
			break;
		case DEACTIVATE_LEDS: //0x03
			bmd_pwm_rgb_led_stop();
			break;
		case ACCEL_STREAM_START: //0x06
			bmd_accel_enable(true);
			bmd_accel_enable_interrupt(true, accel_data_handler);
			break;
		case ACCEL_STOP: //0x09
			bmd_accel_enable(false);
			bmd_accel_enable_interrupt(false, NULL);
			break;
        case SOFT_RESET:
            sd_ble_gap_disconnect( p_bmdeval->conn_handle, 
                BLE_HCI_REMOTE_USER_TERMINATED_CONNECTION );
            softdevice_handler_sd_disable();
            nrf_delay_ms( 500 );
            NVIC_SystemReset(); 
            break;
		default:
			//do nothing
			break;
	}
}

/**@brief Function for handling data ready interrupts from the accelerometer.
 */
static void accel_data_handler(void)
{
    accel_data_t sample;
    bmd_accel_data_get(&sample);
    ble_bmdeval_on_accel_change(mp_bmdeval, &sample);
}
