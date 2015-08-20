/** @file
 * 
 * @brief PWM module for use with the BMD-200 eval board.
 *
 * @details This module implements the PWM on the BMD-200 module and provides an
 * example on implementing PWM for the RGB LED on on the BMD-200 EVAL Board.
 */
#include <stdint.h>
#include "ble_bmdeval.h"

/**
 * @{
 * @ingroup rgb_pwm
 *
 * @brief
 * Function: PWM_RGB_Init
 *
 * Description: Initialize modules required for driving PWM on a RGB LED.
 *
 * Assumptions: Defines in pwm_rgb.c have been set to desired values
 *
 * @param[in] Pointer to BLE service structure
 *
 * @param[out] void
 */
void PWM_RGB_Init(ble_bmdeval_t * p_bmdeval);

/**
 * @brief
 * Function: PWM_RGB_Start
 *
 * Description: Used to set PWM values for and activate RGB LED.
 *
 * Assumptions: Defines in pwm_rgb.c have been set to desired values
 *
 * @param[in] rgb_data : This is made up of three uint8 and is can be treated as a
 * standard 3 byte hexadecimal RGB representation.
 *
 * @param[in] p_bmdeval : Pointer to Bmdeval service structure.
 *
 * @param[out] void
 */
void PWM_RGB_Start(ble_bmdeval_rgb_t * rgb_data, ble_bmdeval_t * p_bmdeval);

/**
 * @brief
 * Function: PWM_RGB_Stop
 *
 * Description: Deinitialize modules required for driving PWM on a RGB LED.
 *
 * Assumptions: Defines in pwm_rgb.c have been set to desired values. PWM previously initialized
 *
 * @param[in] void
 *
 * @param[out] void
 */
void PWM_RGB_Stop(void);

/** @} */
