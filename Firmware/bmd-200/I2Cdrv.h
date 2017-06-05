/** @file
 * 
 * @brief I2C driver module for use with the BMD-200 eval board.
 *
 * @details This module implements basic I2C interface built on top of the Nordic Two Wire Interface. 
 */

#ifndef I2CDRV_H
#define I2CDRV_H

#include <stdint.h>
#include <stddef.h>
#include <stdbool.h>

/**
 * @brief
 * Function: I2CInit
 *
 * Description: Initializes the I2C bus
 *
 * Assumptions: None
 *
 * @param[in] void
 *
 * @param[out] bool : true - initialization successful <BR>
 *                    false - initialization failure
 *                          
 */
bool I2CInit(void);

/**
 * @brief
 * Function: I2CWriteReg
 *
 * Description: Writes byte value to register via I2C
 *
 * Assumptions: None
 *
 * @param[in]  device_addr: device slave I2C address
 * @param[in]  reg_addr: device register address
 * @param[in]  reg_val: register value to write
 *
 * @param[out] void
 */
void I2CWriteReg(uint8_t device_addr, uint8_t reg_addr, uint8_t reg_val);

/**
 * @brief
 * Function: I2CReadReg
 *
 * Description: Reads byte value from data register via I2C
 *
 * Assumptions: None
 *
 * @param[in] device_addr: device slave I2C address
 * @param[in] reg_addr: device register address
 * @param[in] *reg_val: register value to read value to
 * @param[in] num_bytes : number of bytes to read
 *
 * @param[out] void
 */
void I2CReadReg(uint8_t device_addr, uint8_t reg_addr, uint8_t *reg_val, uint8_t num_bytes);

#endif
