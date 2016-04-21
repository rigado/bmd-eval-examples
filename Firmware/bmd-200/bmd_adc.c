/** @file
 * 
 * @brief ADC module for use with the BMD-200 eval board.
 *
 * @details This module implements the ADC on the ARM processor to read 8-bit Analog values
 * from the BMD-200 EVAL board Ambilight sensor. The module is set to take multiple readings
 * and average them.
 */

#include <stdbool.h>
#include <stdint.h>
#include <stdio.h>
#include "nrf.h"
#include "nrf_adc.h"
#include "bmd_adc.h"
#include "boards.h"
#include "app_error.h"
/**
 * @defgroup adc_module ADC Module
 * @{
 * @ingroup adc_module
 * @brief All ADC reading specific items
 * @details
 */
volatile	uint32_t 	    adc_sample 			    = 0;				/**< Used for storing ADC values once they have been read. */
volatile	uint32_t	    adc_result			    = 0;				/**< Used for storing the most recent result of a full ADC conversion. */
static 	 	uint8_t		    sample_counter 	        = 0;				/**< Counter used to keep track of how many ADC readings have occured. */
static 	 	ble_bmdeval_t * adc_bmdeval;								/**< Pointer to bmd_eval structure. */
            bool			conversion_complete 	= false;			/**< Flag for indicating when the correct numer of ADC readings have occured. */
static 		bool			initilized			    = false;			/**< Flag for indicating when the adc module is initialized. */
static 		bool			streaming				= false;			/**< Flag for indicating when streaming mode is active */

static void adc_event_handler(uint8_t adc_value);

#define	BMD200_AMBILIGHT_PIN		NRF_ADC_CONFIG_INPUT_7 		/**< The BMD-200 EVAL board's ambilight sensor is connected to this input. */
#define NO_OF_SAMPLES				4							/**< The number of samples that will be taken to be averaged. */
#ifndef NRF_APP_PRIORITY_HIGH
#define NRF_APP_PRIORITY_HIGH 	    1							/**< ADC interrupt priority */
#endif

/**
 * @brief ADC interrupt handler.
 */
void ADC_IRQHandler(void)
{
		nrf_adc_conversion_event_clean();
    adc_sample = adc_sample + nrf_adc_result_get();
		sample_counter++;
		if(sample_counter < NO_OF_SAMPLES)
		{
			nrf_adc_start();
		}
		else
		{
			adc_result = (uint8_t)(adc_sample / NO_OF_SAMPLES);
		}
}


/**
 * @brief ADC initialization.
 */
void ADC_Init(ble_bmdeval_t * p_bmdeval)
{
	
  const nrf_adc_config_t nrf_adc_config = NRF_ADC_CONFIG_CUSTOM;
	adc_bmdeval = p_bmdeval;
	
  // Initialize and configure ADC
  nrf_adc_configure( (nrf_adc_config_t *)&nrf_adc_config);
  nrf_adc_input_select(BMD200_AMBILIGHT_PIN);
  nrf_adc_int_enable(ADC_INTENSET_END_Enabled << ADC_INTENSET_END_Pos);
  NVIC_SetPriority(ADC_IRQn, NRF_APP_PRIORITY_HIGH);
  NVIC_EnableIRQ(ADC_IRQn);
	initilized = true;
	
	ADC_Value_Start();
}
/**
 * @brief ADC deinitialization.
 */
void	ADC_Deinit(void)
{
		nrf_adc_int_disable(ADC_INTENSET_END_Enabled << ADC_INTENSET_END_Pos);
		NVIC_DisableIRQ(ADC_IRQn);
		nrf_adc_stop();
		initilized = false;
}
/**
 * @brief Function for starting the four ADC readings
 */
void ADC_Value_Start(void)
{
		adc_sample 			= 0;
		sample_counter 	= 0;
    nrf_adc_start();
}
/**
 * @brief Function for retreiving the averaged ADC reading.
 * NOTE: Calling this before calling Value_Start will result in a infinite loop
 */
void ADC_Value_Get(void)
{
	adc_event_handler(adc_result);
}

//ADC event handler module.
static void adc_event_handler(uint8_t adc_value)
{
		uint32_t err_code;
		err_code = ble_bmdeval_on_adc_change(adc_bmdeval, adc_value);
		
		if (err_code != NRF_SUCCESS &&
				err_code != BLE_ERROR_INVALID_CONN_HANDLE &&
        err_code != NRF_ERROR_INVALID_STATE)
    {
				APP_ERROR_CHECK(err_code);
    }
}

//Function for controling streaming flag
void ADC_Streaming_Mode(bool on_off)
{
	if(initilized == true)
	{
		streaming = on_off;
	}
	if(initilized == false)
	{
		//Do nothing. must be initialized from main first.
	}
	return;
}

void ADC_Streaming_Handler(void)
{
	if(streaming == false)
	{
		//Do nothing if streaming is not active.
		return;
	}
	ADC_Value_Start();
	ADC_Value_Get();
	
}

/** @} */
