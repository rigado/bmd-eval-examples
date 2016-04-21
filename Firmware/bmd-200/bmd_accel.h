/** @file
 * 
 * @brief I2C module for use with the BMD-200 eval board.
 *
 * @details This module implements the accelerometer on the BMD-200 EVAL board and provides an
 * example on implementing I2C on an BMD-200 module.
 */

#include <stdint.h>
#include "ble_bmdeval.h"

/**
 * @defgroup accel_drv Accelerometer Driver
 * @{
 * @ingroup accel_drv
 * @brief All accelerometer driving items
 * @details This module contains the top-level functions used to drive the accelerometer.
 */

/**
 * Function: Accel_Init
 *
 * @brief Initialize accelerometer general configuration to be ready
 *							for application specific configuration.
 *
 * Assumptions: Device has interrupt line for i2c that is configured to be active low.
 *
 * @param[in] *p_bmdeval : Pointer to bluetooth service structure that will be used to transmit data
 *
 * @param[out] void
 */
void Accel_Init(ble_bmdeval_t * p_bmdeval);

/**
 * Function: Accel_Deinit
 *
 * @brief Deinitialize accelerometer for low power use while not in use.
 *
 * Assumptions: Device has been previously initialized.
 *
 * @param[in] void
 *
 * @param[out] void
 */
void Accel_Deinit(void);

/**
 * Function: Accel_Data_Get
 *
 * @brief Accessor method to call static event handler. Reads a single
 *							reading from the accelerometer to attached bluetooth service.
 *
 * Assumptions: Device has been initialized. 
 *
 * @param[in] void
 *
 * @param[out] void
 *
 * NOTE: All i2c devices should be Deinitialized when not in use.
 */
void Accel_Data_Get(void);

/**
 * Function: Accel_Data_Get_Mode
 *
 * @brief Function to get value of data_mode.
 *
 * Assumptions: Device has been initialized. 
 *
 * @param[in] void
 *
 * @param[out] void
 *
 * NOTE: All i2c devices should be Deinitialized when not in use.
 */
uint8_t Accel_Data_Get_Mode(void);

/**
 * Function: Accel_Streaming_Handler
 *
 * @brief Functions like Accel_Data_Get except this relies on the state
 *							of the streaming flag. Data will only be sent to the BLE service
 *							if the streaming flag is set.
 *
 * Assumptions: Device has been initialized. Streaming flag is externaly controlled by
 * 							Accel_Streaming_Mode. 
 *
 * @param[in] void
 *
 * @param[out] void
 */
void Accel_Streaming_Handler(void);

/**
 * Function: Accel_Streaming_Mode
 *
 * @brief Function for controling the streaming flag.
 *
 * Assumptions: None.
 *
 * @param[in] on_off : value to control the streaming flag.
 *
 * @param[out] void
 */
void Accel_Streaming_Mode(bool on_off);

/**
 * Function: Accel_Config_Data_Ready
 *
 * @brief Function for configuring accelerometer into Data Ready mode.
 *							See MMA8652 datasheet for details.
 *
 * Assumptions: Accelerometer has been configured
 *
 * @param[in] void
 *
 * @param[out] void
 */
void Accel_Config_Data_Ready(void);

/**
 * Function: Accel_Config_Transient
 *
 * @brief Function for configuring accelerometer into Transient mode (Vibration/Movement detection.)
 *							See MMA8652 datasheet for details.
 *
 * Assumptions: Accelerometer has been configured
 *
 * @param[in] : void
 *
 * @param[out] : void
 */
void Accel_Config_Transient(void);

/**
 * @brief
 * Function: Accel_Config_Double_Tap
 *
 * Description:  Enable double tap interrupt
 *
 * Assumptions: none
 *
 * @param[in] void
 *
 * @param[out] void
 */
 void Accel_Config_Double_Tap(void);
 
 /**
 * @brief
 * Function: Accel_Config_Motion
 *
 * Description:  Enable motion interrupt
 *
 * Assumptions: none
 *
 * @param[in] void
 *
 * @param[out] void
 */
 void Accel_Config_Motion(void);

 /**
 * @brief
 * Function: Accel_Config_Orientation
 *
 * Description:  Enable Orientation interrupt
 *
 * Assumptions: none
 *
 * @param[in] void
 *
 * @param[out] void
 */
 void Accel_Config_Orientation(void);

/** @} */
