/** @file
 * 
 * @brief I2C driver module for use with the BMD-200 eval board.
 *
 * @details This module implements basic I2C interface built on top of the Nordic Two Wire Interface. 
 */
#include "I2Cdrv.h"
#include "twi_master.h"

/**
 * @{
 * @ingroup accel_cfg
 */

bool I2CInit()
{       
    return(twi_master_init());
}

void I2CWriteReg(uint8_t device_addr, uint8_t reg_addr, uint8_t reg_val)
{
    uint8_t data_buff[2];
    
    data_buff[0] = reg_addr;
    data_buff[1] = reg_val;
    twi_master_transfer(device_addr, &data_buff[0], 2, true);  
}

void I2CReadReg(uint8_t device_addr, uint8_t reg_addr, uint8_t *reg_val, uint8_t num_bytes)
{   
    /* First send register address */
    twi_master_transfer((device_addr<<1), &reg_addr, 1, false);         /* do not send stop bit */
    twi_master_transfer(((device_addr<<1)|0x01), reg_val, num_bytes, true);     /* complete transfer with stop bit */
}
/** @} */
