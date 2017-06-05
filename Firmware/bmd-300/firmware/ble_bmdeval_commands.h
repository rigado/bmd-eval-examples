/** @file ble_bmdeval_commands.h
*
* @brief This modules defines all commands handled by ble_bmdeval_handler.  New
*        commands should be added here.
*
* @par
* COPYRIGHT NOTICE: (c) Rigado
* All rights reserved. 
*
* Source code licensed under Software License Agreement in license.txt.
* You should have received a copy with purchase of BMD series product 
* and with this repository.  If not, contact modules@rigado.com.
*/

#ifndef __BLE_BMDEVAL_COMMANDS_H__
#define __BLE_BMDEVAL_COMMANDS_H__

#ifdef __cplusplus
extern "C" {
#endif
    
/**
 * @defgroup Control_Point_Commands Control Point Commands
 * @{ 
 * @ingroup Control_Point_Commands
 * @brief   Conrol point command codes. Writing these codes to the control
 *          point characteristic will result in the following behavours.
 */

/**< For Debug use only. */
#define DEBUG_RESERVED					0x00	

/**< Begins ADC streaming. Values are written to the ADC characteristic */

/**< Starts ADC streaming. Resulting value is written to the 
ADC characteristic */
#define	ADC_STREAM_START				0x01

/**< Stops ADC streaming. Resulting value is written to the 
ADC characteristic */
#define ADC_STREAM_STOP					0x02										

/**< Turns off and deinitializes LEDs. */
#define DEACTIVATE_LEDS					0x03	    								

/**< Starts auto streaming of Acclerometer. Resulting values are written to the 
Accel characteristic. Will continue streaming data in this mode until 
reconfigured or ACCEL_STOP is received. */
#define ACCEL_STREAM_START				0x06

/**< Stops the Acceleromerter data streaming. */
#define ACCEL_STOP						0x09

/**< Soft-Reset command. Resets application and softdevice. */
#define SOFT_RESET                      0xE7D6FCA1

#ifdef __cplusplus
}
#endif

#endif
