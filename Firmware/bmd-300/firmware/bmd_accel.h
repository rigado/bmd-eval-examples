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
 * @defgroup bmd_accel Accelerometer Driver
 * @{
 * @ingroup bmd_accel_drv
 * @brief Accelerometer interface
 */

/**
 * Function: bmd_accel_init
 *
 * @brief Initialize accelerometer general configuration to be ready
 *							for application specific configuration.
 *
 * Assumptions: Device has interrupt line for i2c that is configured to be active low.
 *
 * @param[in] *p_bmdeval : Pointer to bluetooth service structure that will be used to transmit data
 *
 * @returns initialized state
 */
bool bmd_accel_init(ble_bmdeval_t * const p_bmdeval);

/**
 * Function: bmd_accel_deinit
 *
 * @brief Deinitialize accelerometer for low power use while not in use.
 *
 * Assumptions: Device has been previously initialized.
 *
 * @param[in] void
 *
 * @returns true if successful; false if not initialized or unsuccessful
 */
bool bmd_accel_deinit(void);

/**
 * Function: accel_data_get
 *
 * @brief Accessor method to call static event handler. Reads a single
 *							reading from the accelerometer to attached bluetooth service.
 *
 * Assumptions: Device has been initialized. 
 *
 * @param[out] p_data : current accelerometer x,y, z axis readings 
 *
 * @returns NRF_SUCCESS if successful
 *          NRF_ERROR_INVALID_STATE if not initialized
 *          NRF_ERROR_BUSY if i2c transaction fails
 *
 * NOTE: All i2c devices should be Deinitialized when not in use.
 */
uint32_t bmd_accel_data_get(accel_data_t * const p_data);

/**
 * Function: bmd_accel_enable
 *
 * @brief Enables accelerometer at 12.5 Hz rate, +/- 2G scale, 8-bit data
 *
 * @param[in] state : if true, enables accelerometer, if false, disables it
 *
 * @returns NRF_SUCCESS if successful
 *          NRF_ERROR_INVALID_STATE if not initialized
 *
 */
uint32_t bmd_accel_enable(bool state);

/**
 * Function: bmd_accel_is_enabled
 *
 * @brief Gets the enabled state of the accelerometer
 *
 * @note This performs an I2C read to get the state of the active bit
 *
 * @returns true if enabled, false otherwise
 */
bool bmd_accel_is_enabled(void);

/**
 * Function: bmd_accel_is_data_ready
 *
 * @brief Gets the data ready bit of the state register
 *
 * @returns true if data is waiting, false otherwise
 */
bool bmd_accel_is_data_ready(void);

/**
 * Function: bmd_accel_enable_interrupt
 *
 * @brief Enables or disabled the data ready interrupt on ACCEL_INT_1
 *
 * @param[in] state : if true, enables accelerometer, if false, disables it
 * @param[in] accel_data_ready_cb : function to call when data ready pin
 *              activates
 *
 * @returns NRF_SUCCESS if successful
 *          NRF_ERROR_INVALID_STATE if not initialized or accel not enabled
 */
uint32_t bmd_accel_enable_interrupt(bool state, void (*accel_data_ready_cb)(void));

/**
 * Function: bmd_accel_is_int_enabled
 *
 * @brief Returns the enable state of the accel data ready interrupt
 *
 * @note Returns state of internal flag of module, no I2C transaction performed
 *
 * @returns true if enabled, false if not
 */
bool bmd_accel_is_int_enabled(void);

/** @} */
