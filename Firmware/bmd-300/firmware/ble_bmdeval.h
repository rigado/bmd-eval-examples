/* Copyright (c) 2013 Nordic Semiconductor. All Rights Reserved.
 *
 * Use of this source code is governed by a BSD-style license that can be
 * found in the license.txt file.
 */

/** @file ble_bmdeval.h
 * 
 * @brief ble_bmdEval service module header file. 
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
 * \section ctrl_pt Control Point
 * <P>
 * The control point characteristic is used to control various elements of the BMD-200 Eval board.
 * The control point characteristic consists of a single command byte. Controling the board can be achieved by
 * writing one of the commands listed below to the characteristic.
 * <UL>
 *	<LI> 	0x00 : Debug_Reserved. Not currently implemented
 *	<LI>	0x01 : ADC_Stream_Start. Begins ADC streaming. Resulting value is written to the ADC characteristic.
 *	<LI>	0x02 : ADC_Stream_Stop. Stops ADC streaming.
 *	<LI>	0x03 : Deactivate_LEDs. Turns off LEDs.
 *	<LI>	0x06 : Accel_Stream_Start. Starts auto streaming of Acclerometer. Resulting values are written to the Accel characteristic. Will continue streaming data in this mode until reconfigured or ACCEL_STOP is received.
 *	<LI>	0x09 : Accel_Stop. Stops the Acceleromerter data streaming.
 * </UL>
 * \section button	Button Characteristic
 * <P>
 *	The button characteristic updates itself to reflect the current state of the buttons. The lower nibble represents the state of the first button
 * while the upper nibble represents the state of the second button. (1 = pressed, 0 = not pressed)
 * \section rgb_led	RGB Characteristic
 * <P>
 * The RGB LED characteristic consists of a 3 byte RGB hexidecimal datatype similar to standard RGB color representations.
 * (Ordering: Red-Byte, Green-Byte, Blue-Byte)
 * \section adc_char ADC Characteristic
 * <P>
 * The ADC characteristic consists of a single byte that notifies on updates. When ADC data is updated, it is displayed here. 
 * \section accel_char Accelerometer Characteristic
 * <P>
 * The Accelerometer characteristic consists of four bytes. The lowest byte contains a mode indicator byte. The value in this byte
 * matches the corresponding accelerometer mode in the control point characteristic. The upper three bytes contain the 8-bit XYZ data
 * while in data streaming mode. While in other modes, the upper byte is the contents of the corresponding status registers for the current mode.
 *
 * @par
 * COPYRIGHT NOTICE: (c) Rigado
 * All rights reserved. 
 *
 * Source code licensed under Software License Agreement in license.txt.
 * You should have received a copy with purchase of BMD series product 
 * and with this repository.  If not, contact modules@rigado.com.
 */

#ifndef ble_bmdeval_H__
#define ble_bmdeval_H__

#include <stdint.h>
#include <stdbool.h>
#include "ble.h"
#include "ble_srv_common.h"
#include "bmd_pwm_rgb_led.h"

/**
 * @defgroup cust_service BMD Eval Service 
 * @{
 * @ingroup cust_service
 * @brief Details on the BMD Eval service.
 */
 
/**
* @defgroup ble_bmdeval_uuid BMDEval UUIDs
* @{
* @ingroup ble_bmdeval_uuid
*/ 
  //84 26 e2 e9 7b ab 89 95 90 46 8d 41 00 00 DB 50
  
/**< Randomly Generated Device base UUID */
#define BMDEVAL_UUID_BASE { 0x84, 0x26, 0xE2, 0xE9, 0x7B, 0xAB, 0x89, 0x95, \
                            0x90, 0x46, 0x8D, 0x41, 0x00, 0x00, 0xDB, 0x50 } 
#define BMDEVAL_UUID_SERVICE 		0x1523		/**< Service UUID */
#define BMDEVAL_UUID_BUTTON_CHAR 	0x1524 		/**< Button Characteristic UUID */
#define BMDEVAL_UUID_LED_CHAR 		0x1525		/**< LED Characteristic UUID */
#define BMDEVAL_UUID_ADC_CHAR 		0x1526		/**< Analog to Digital converter Characteristic UUID */
#define BMDEVAL_UUID_CTRL_CHAR 		0x1527		/**< Control Point Characteristic UUID */
#define BMDEVAL_UUID_ACCEL_CHAR 	0x1528		/**< Accelerometer Characteristic UUID */
/** @} */

/** @brief Struct to contain the LED RGB values. Values are formatted as standard 3 byte hexadecimal RGB representations */
typedef struct						
{
	uint8_t 	red_value;			/**< 1-byte red value */
	uint8_t 	green_value;		/**< 1-byte green value */
	uint8_t		blue_value;			/**< 1-byte blue value */
} ble_bmdeval_rgb_t;

/** @brief Struct to contain the accelerometer XYZ values. 
 *  @details This struct contains X, Y, and Z values from the accelerometer as well as a accelerometer datatype indicator byte (mode)
 *  The value in 'mode' matches the control point command used to configure accelerometer.
 */
typedef struct						
{
	uint8_t		x_value;		/**< 1-byte X-axis value*/
	uint8_t		y_value;		/**< 1-byte Y-axis value*/
	uint8_t		z_value;		/**< 1-byte Z-axis value*/
}	accel_data_t;

// Forward declaration of the ble_bmdeval_t type. 
typedef struct ble_bmdeval_s ble_bmdeval_t;

typedef void (*ble_bmdeval_led_write_handler_t)(ble_bmdeval_t * p_bmdeval, 
                                                bmd_pwm_rgb_color_t * color);

typedef void (*ble_bmdeval_ctrl_write_handler_t)(ble_bmdeval_t * p_bmdeval, 
                                                 uint32_t write_data);

/** @brief BMD EVAL service handler initialization structure. */
typedef struct
{
    ble_bmdeval_led_write_handler_t 	led_write_handler;            /**< Event handler to be called when LED characteristic is written. */
	ble_bmdeval_ctrl_write_handler_t	ctrl_write_handler;						/**< Event handler to be called when Control point characteristic is written. */
} ble_bmdeval_init_t;


/** @brief BMD EVAL Service structure. This contains various status information for the service. */
typedef struct ble_bmdeval_s
{
    uint16_t                    		service_handle;					/**< Service structure handle */
    ble_gatts_char_handles_t    		led_char_handles;				/**< LED characteristic handles */
    ble_gatts_char_handles_t    		button_char_handles;		/**< Button characteristic handles */
	ble_gatts_char_handles_t			adc_char_handles;				/**< ADC characteristic handles */
	ble_gatts_char_handles_t			ctrl_char_handles;			/**< Control point characteristic handles */
	ble_gatts_char_handles_t			accel_char_handles;			/**< Accelerometer characteristic handles */
    uint8_t                     		uuid_type;							/**< Device UUID Type identifier */
    uint16_t                    		conn_handle;						/**< Service connection handle. */
    ble_bmdeval_led_write_handler_t 	led_write_handler;		/**< Event handler to be called when LED characteristic is written. */
	ble_bmdeval_ctrl_write_handler_t 	ctrl_write_handler;		/**< Event handler to be called when Control point characteristic is written. */
} ble_bmdeval_t;

/**@brief Function for initializing the BMD Eval Service.
 *
 * @param[out]  p_bmdeval       BMD Eval Service structure. This structure will have to be supplied by
 *                          		the application. It will be initialized by this function, and will later
 *                          		be used to identify this particular service instance.
 * @param[in]   p_bmdeval_init  Information needed to initialize the service.
 *
 * @return      NRF_SUCCESS on successful initialization of service, otherwise an error code.
 */
uint32_t ble_bmdeval_init(ble_bmdeval_t * p_bmdeval, const ble_bmdeval_init_t * p_bmdeval_init);

/**@brief Function for handling the Application's BLE Stack events.
 *
 * @details Handles all events from the BLE stack of interest to the BMD Eval Service.
 *
 *
 * @param[in]   p_bmdeval     BMD Eval Service structure.
 * @param[in]   p_ble_evt  		Event received from the BLE stack.
 */
void ble_bmdeval_on_ble_evt(ble_bmdeval_t * p_bmdeval, ble_evt_t * p_ble_evt);

/**@brief Function for sending a button state notification.
 *
 * @details Handles all button change events and updates the button char handle
 *
 *
 * @param[in]		p_bmdeval 		BMD Eval service structure pointer.
 * @param[in]		button_state	Value indicating the current button state. (This is the value to be written to the characteristic.)
 */
uint32_t ble_bmdeval_on_button_change(const ble_bmdeval_t * p_bmdeval, uint8_t button_state);

/**@brief Function for sending an ADC value change notification.
 *
 * @details Handles all analog-to-digital converter events and updates the ADC char handle
 *
 *
 * @param[in]		p_bmdeval 		BMD Eval service structure pointer
 * @param[in]		adc_value			Value of current ADC reading. (This is the value to be written to the characteristic.)
 */
uint32_t ble_bmdeval_on_adc_change(const ble_bmdeval_t * p_bmdeval,  uint8_t adc_value);

/**@brief Function for sending an accelerometer value change notification.
 *
 * @details Handles all Accelerometer events and updates the Accel. char handle.
 *
 *
 * @param[in]		p_bmdeval 		BMD Eval service structure pointer
 * @param[in]		accel_value		Value of current accelerometer reading. (This is the value to be written to the characteristic.)
 */
uint32_t ble_bmdeval_on_accel_change(const ble_bmdeval_t * p_bmdeval,  const accel_data_t * const p_accel_value);

/**@brief Function for sending a control point value change notification.
 *
 * @details Handles all Control point write events and updates the Accel. char handle.
 *
 *
 * @param[in]		p_bmdeval 		BMD Eval service structure pointer
 * @param[in]		ctrl_value		Value of ctrl point to be written to characteristic. (This is the value to be written to the characteristic.)
 */
uint32_t ble_bmdeval_on_ctrl_change(const ble_bmdeval_t * p_bmdeval,  uint8_t ctrl_value);

/**@brief Function for sending a control point value change notification.
 *
 * @details Handles all Control point write events and updates the Accel. char handle.
 *
 *
 * @param[in]		p_bmdeval 		BMD Eval service structure pointer
 * @param[in]		pwm_value		Value of pwm to be written to characteristic. (This is the value to be written to the characteristic.)
 */
uint32_t ble_bmdeval_on_pwm_change(const ble_bmdeval_t * p_bmdeval,  ble_bmdeval_rgb_t pwm_value);

#endif // ble_bmdeval_H__

/** @} */
