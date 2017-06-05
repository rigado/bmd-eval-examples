/** @file
 * 
 * @brief Module for assisting in I2C communications with the MMA8652 accelerometer.
 *
 * @details This module is capable of performing chip specific operations such as setup. This module does not
 * implement every feature of the MMA8652, but does provide a framework to implement additional functionality.
 * For more details on custom setup parameters, see the datasheet for the MMA8652. 
 */
#ifndef _MMA8652X_H_
#define _MMA8652X_H_

#ifdef __cplusplus
extern "C" {
#endif

#include <stdbool.h>
#include <stdint.h>

#define USE_TRIGGER

#define MMA8652_ADDR                          0x1D	                    
                                                /**<  Accelerometer I2C address */
#define MMA8652_READ_ADDR                     ((MMA8652_ADDR<<1)|0x01)	/**<  Accelerometer read address */
#define MMA8652_WRITE_ADDR                    (MMA8652_ADDR<<1)			/**<  Accelerometer write address */

#define MMA8652_DATA_STATUS_REG_ADDR          0x00	/**< Data Status register*/
#define MMA8652_OUT_X_MSB                     0x01	/**< X-axis most significant bit data register*/
#define MMA8652_OUT_X_LSB                     0x02	/**< X-axis least significant bit data register*/
#define MMA8652_OUT_Y_MSB                     0x03  /**< Y-axis most significant bit data register*/
#define MMA8652_OUT_Y_LSB                     0x04	/**< Y-axis least significant bit data register*/
#define MMA8652_OUT_Z_MSB                     0x05	/**< Z-axis most significant bit data register*/
#define MMA8652_OUT_Z_LSB                     0x06	/**< Z-axis least significant bit data register*/

#define MMA8652_INT_SOURCE_REG_ADDR           0x0C	/**< Interrupt Source register */
#define MMA8652_ID_REG_ADDR                   0x0D	/**< Device ID register. Should always read '4A' */
#define MMA8652_ID_VAL                        0x4A  /* factory set device ID */
#define MMA8652_TRIG_CFG_REG_ADDR             0x0A	/**< Trigger Configuration register */
#define MMA8652_XYZ_DATA_CFG_REG_ADDR         0x0E	/**< XYZ Data Configuration register */
#define MMA8652_HP_FILTER_CUTOFF_REG_ADDR     0x0F  /**< High-Pass Filter Cutoff register */

#define MMA8652_PL_STATUS_REG_ADDR            0x10  /**< Portrait/Landscape status register */
#define MMA8652_PL_CFG_REG_ADDR               0x11	/**< Portrait/Landscape configuration register */
#define MMA8652_PL_COUNT_REG_ADDR             0x12  /**< Portrait/Landscape debounce register */
#define MMA8652_PL_BF_ZCOMP_REG_ADDR          0x13  /**< Portrait/Landscape Z-Compensation register */
#define MMA8652_P_L_THS_REG_ADDR              0x14	/**< Portrait/Landscape Threshold register */

#define MMA8652_FF_MT_CFG_REG_ADDR            0x15  /**< Freefall/Motion Configuration register */
#define MMA8652_FF_MT_SRC_REG_ADDR            0x16	/**< Freefall/Motion Source register */
#define MMA8652_FF_MT_THR_REG_ADDR            0x17  /**< Freefall/Motion Threshold register */
#define MMA8652_FF_MT_COUNT_REG_ADDR          0x18	/**< Freefall/Motion Debounce register */

#define MMA8652_TRANSIENT_CFG_REG_ADDR        0x1D	/**< Transient Configuration register */
#define MMA8652_TRANSIENT_SRC_REG_ADDR        0x1E	/**< Transient Source register */
#define MMA8652_TRANSIENT_THS_REG_ADDR        0x1F	/**< Transient Threshold register */
#define MMA8652_TRANSIENT_COUNT_REG_ADDR      0x20	/**< Transient Debounce register */

#define MMA8652_PULSE_CFG_REG_ADDR            0x21  /**< Pulse Configuration register */
#define MMA8652_PULSE_SRC_REG_ADDR            0x22	/**< Pulse Source register */
#define MMA8652_PULSE_THSX_REG_ADDR           0x23	/**< Pulse X Threshold register */
#define MMA8652_PULSE_THSY_REG_ADDR           0x24	/**< Pulse Y Threshold register */
#define MMA8652_PULSE_THSZ_REG_ADDR           0x25	/**< Pulse Z Threshold register */
#define MMA8652_PULSE_TMLT_REG_ADDR           0x26	/**< Pulse Window 1 register */
#define MMA8652_PULSE_LTCY_REG_ADDR           0x27	/**< Pulse Latency register */
#define MMA8652_PULSE_WIND_REG_ADDR           0x28	/**< Pulse Window 2 register */

/* MMA8652 Control registers */
#define MMA8652_CTRL_REG1_ADDR                0x2A  /**< Device Configuration Register 1 */
#define MMA8652_CTRL_REG2_ADDR                0x2B  /**< Device Configuration Register 2 */
#define MMA8652_CTRL_REG3_ADDR                0x2C  /**< Device Configuration Register 3 */
#define MMA8652_CTRL_REG4_ADDR                0x2D  /**< Device Configuration Register 4 */
#define MMA8652_CTRL_REG5_ADDR                0x2E 	/**< Device Configuration Register 5 */

/* MMA8652 Axis offset registers */
#define MMA8652_OFF_X_REG_ADDR                0x2F	/**< X-axis Offset register */ 
#define MMA8652_OFF_Y_REG_ADDR                0x30	/**< Y-axis Offset register */
#define MMA8652_OFF_Z_REG_ADDR                0x31	/**< Z-axis Offset register */

/**
 * @defgroup accel_cfg Accelerometer Configuration
 * @{
 * @ingroup accel_cfg
 * @brief All accelerometer configuration items.
 * @details This module contains functions required to set-up accelerometer into different modes.
 * for more detailed information, see the MMA8652 datasheet.
 */

/**
 * @brief
 * Function: MMA8652Init
 *
 * Description:  Initialize accelerometer (MMA8652FC)
 *
 * Assumptions: None
 *
 * @param[in] void
 *
 * @param[out] bool : false - device not identified/communication error <BR>
 *                           true - device identified and read from
 */
bool MMA8652Init(void);

/** 
 * @brief
 * Function: MMA8652Deinit
 *
 * Description:  Deinitialize accelerometer (MMA8652FC)
 *
 * Assumptions: None
 *
 * @param[in] void
 *
 * @param[out] void
 */
void MMA8652Deinit(void);

/**
 * @brief
 * Function: MMA8652EnableVibrationMode
 *
 * Description:  Initialize accelerometer to motion Z axis interrupt.  Will set 
 *               INT1 when data is ready to be collected.
 *
 * Assumptions: None
 *
 * @param[in] void
 *
 * @param[out] void
 */
void MMA8652EnableVibrationMode(void);

/**
 * @brief
 * Function: MMA8652EnableOrientationMode
 *
 * Description:  Initializes orientation detection mode
 *
 * Assumptions: None
 *
 * @param[in] void
 *
 * @param[out] void
 */
void MMA8652EnableOrientationMode(void);
  
/**
 * @brief
 * Function: MMA8652Calibration
 *
 * Description:  Calibrates the axis offsets
 *
 * Assumptions: board needs to be mounted and stable
 *
 * @param[in] *p_buffer : pointer to the output offset buffer
 *
 * @param[out] void
 */
void MMA8652Calibration(int8_t *p_buffer);

/**
 * @brief
 * Function: MMA845EnableDataReadyMode
 *
 * Description:  Enable reading XYZ data registers
 *
 * Assumptions: Used only for debugging
 *
 * @param[in] state : enabled if true, disable if false
 *
 * @returns NRF_SUCCESS if successful; error otherwise
 */
uint32_t MMA8652EnableDataReadyMode(bool state);

/**
 * @brief
 * Function: MMA845EnableDoubleTapMode
 *
 * Description:  Enable double tap interrupt
 *
 * Assumptions: none
 *
 * @param[in] void
 *
 * @param[out] void
 */
void MMA8652EnableDoubleTapMode(void);

/**
 * @brief
 * Function: MMA845EnableMotionMode
 *
 * Description:  Enable motion interrupt
 *
 * Assumptions: none
 *
 * @param[in] void
 *
 * @param[out] void
 */
void MMA8652EnableMotionMode(void);

/**
 * @brief
 * Function: MMA8652ReadAllAxisData
 * 
 * Description: Read X, Y, and Z axis data
 *
 * Assumptions: Driver is initialized
 *
 * @param[out] p_accel_data : buffer for axis data
 *
 * @returns none
 */
void MMA8652ReadAllAxisData(int8_t * const p_accel_data);
/** @} */

/**
 * @brief
 * Function: MMA8652ReadReg
 * 
 * Description: Read register value
 *
 * Assumptions: Driver is initialized
 *
 * @param[in] reg : register to read
 *
 * @param[out] p_reg_val : buffer for the register data
 *
 * @param[in] len : len of buffer
 *
 * @returns none
 */
void MMA8652ReadReg(uint8_t reg, uint8_t * const p_reg_val, uint8_t len);
/** @} */

#ifdef __cplusplus
}
#endif

#endif
