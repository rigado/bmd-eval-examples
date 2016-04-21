/** @file
 *
 * @brief ADC module for use with the BMD-200 eval board.
 *
 * @details This module implements the ADC on the ARM processor to read 8-bit Analog values
 * from the BMD-200 EVAL board Ambilight sensor. The module is set to take multiple readings
 * and average them.
 */

#include <stdint.h>
#include "ble_bmdeval.h"
/**
 *	@{
 *	@ingroup adc_module
 */
/** ADC Configuration for the BMD-200 EVAL board. Currently uses ADC in 8-bit mode. */
#define NRF_ADC_CONFIG_CUSTOM { NRF_ADC_CONFIG_RES_8BIT,               \
                                 NRF_ADC_CONFIG_SCALING_INPUT_TWO_THIRDS, \
                                 NRF_ADC_CONFIG_REF_VBG }

/**
 * @details
 * Function: ADC_Value_Start
 *
 * Description: Function for starting the ADC readings
 *
 * Assumptions: ADC has been initialized
 *
 * @param[in] void
 *
 * @param[out] void
 *                          
 */
void  ADC_Value_Start(void);
																 
/**
 * @details
 * Function: ADC_Value_Get
 *
 * Description: Function for retreiving the averaged ADC reading.
 * NOTE: Calling this before calling Value_Start will result in a infinite loop
 *
 * Assumptions: ADC has been initialized
 *
 * @param[in] void
 *
 * @param[out] void
 *                          
 */
void ADC_Value_Get(void);

/**
 * @details
 * Function: ADC_Deinit
 *
 * Description: Function for Deinitializing the ADC module
 *
 * Assumptions: ADC has been initialized
 *
 * @param[in] void
 *
 * @param[out] void
 *                          
 */
void	ADC_Deinit(void);

/**
 * @details
 * Function: ADC_Init
 *
 * Description: Function for Initializing the ADC module
 *
 * Assumptions: None
 *
 * @param[in] p_bmdeval : Pointer to bluetooth service structure that will be used to transmit data.
 *
 * @param[out] void
 *                          
 */
void	ADC_Init(ble_bmdeval_t * p_bmdeval);

void ADC_Streaming_Mode(bool on_off);

void ADC_Streaming_Handler(void);
/** @} */
