/* Source files for handling transactions with the MMA8651x accelerometer on the Rigado BMD-200 Eval board.
Current settings are for 8 bit mode with a data-ready interrupt at low power mode. */

/** @file
 * 
 * @brief I2C module for use with the BMD-200 eval board.
 *
 * @details This module implements the accelerometer on the BMD-200 EVAL board and provides an
 * example on implementing I2C on an BMD-200 module.
 */

#include <nrf.h>
#include <nrf_soc.h>

#include "bmd_accel.h"
#include "nrf_gpio.h"
#include "nrf_drv_gpiote.h"
#include "MMA8652.h"
#include "I2Cdrv.h"
#include "app_error.h"

#define	ACCEL_IRQ_PIN						4U   				/**< Accelerometer interrupt pin for the BMD-200*/

static  accel_data_t 	accel_data;			                    /**< Accelerometer data stored here */																						
static 	bool			initilized	=		false;				/**< Flag for indicating if the accelerometer has been initialized*/
static 	bool			streaming	=    	false;				/**< Flag for indicating if the accelerometer is streaming. */
static  bool			data_ready	=		false;				/**< Flag for indicating if data streaming is enabled. */

static 	ble_bmdeval_t * accel_bmdeval;						    /**< Pointer to bmd_eval structure. */
static  uint32_t 	    err_code;
static  uint8_t         current_mode = 0x00;

static void accel_event_handler(accel_data_t data_to_send);


/* Sends the new accelerometer to the BMDEVAL service */
static void accel_event_handler(accel_data_t data_to_send)
{
	if (data_ready == false)
	{
		return;
	}
	err_code = ble_bmdeval_on_accel_change(accel_bmdeval, data_to_send);
	data_ready = false;
		
	if (err_code != NRF_SUCCESS &&
		err_code != BLE_ERROR_INVALID_CONN_HANDLE &&
        err_code != NRF_ERROR_INVALID_STATE)
    {
        APP_ERROR_CHECK(err_code);
    }
	return;
}

/* Function used in conjunction with a timer or interrupt can be used to poll the accelerometer data. */
void Accel_Streaming_Handler(void)
{
	
	if(streaming == false) //Dont continue if the streaming flag is cleared.
	{
		return;
	}
	
	uint8_t reg_status = 0;
	
	I2CReadReg(MMA8652_ADDR, MMA8652_INT_SOURCE_REG_ADDR, &reg_status, 1);  /* REG 0x0C Get the interrupt source */
		
	if(reg_status & 0x01) //Checks for data ready interrupt.
	{
		uint8_t reg_data[3];
		
		do
		{
			I2CReadReg(MMA8652_ADDR, MMA8652_OUT_X_MSB, &reg_data[0], 3);        /* Read X, Y, Z data buffers */
			I2CReadReg(MMA8652_ADDR, MMA8652_INT_SOURCE_REG_ADDR, &reg_status, 1);  /* REG 0x0C Get the interrupt source */		
		} while(reg_status & 0x01); /* Read data in until the interrupt is cleared */
		
		accel_data.x_value = reg_data[0];
		accel_data.y_value = reg_data[1];
		accel_data.z_value = reg_data[2];
		current_mode = 0x06;
			
		data_ready = true;
		
		accel_event_handler(accel_data);
	}
		
	else if (reg_status & 0x20) //Checks for a transient interrupt.
	{
		I2CReadReg(MMA8652_ADDR, MMA8652_TRANSIENT_SRC_REG_ADDR, &reg_status, 1);      /* REG 0x1E Get the motion interrupt source and clears interrupt*/	

		accel_data.x_value = reg_status;
		accel_data.y_value = 0x00;
		accel_data.z_value = 0x00;
		current_mode = 0x04;
			
		data_ready = true;
		
		accel_event_handler(accel_data);
	}
	else if (reg_status & 0x08) //Checks for a pulse interrupt.
	{
		I2CReadReg(MMA8652_ADDR, MMA8652_PULSE_SRC_REG_ADDR, &reg_status, 1);      /* REG 0x1E Get the pulse source and clears interrupt*/	

		accel_data.x_value = reg_status;
		accel_data.y_value = 0x00;
		accel_data.z_value = 0x00;
		current_mode = 0x05;
			
		data_ready = true;
		
		accel_event_handler(accel_data);
	}
	else if (reg_status & 0x04) //Checks for a motion interrupt.
	{
		I2CReadReg(MMA8652_ADDR, MMA8652_FF_MT_SRC_REG_ADDR, &reg_status, 1);      /* Get the Motion source and clears interrupt*/	

		accel_data.x_value = reg_status;
		accel_data.y_value = 0x00;
		accel_data.z_value = 0x00;
		current_mode = 0x07;
			
		data_ready = true;
		
		accel_event_handler(accel_data);
	}
	else if (reg_status & 0x10) //Checks for an orientation interrupt.
	{
		I2CReadReg(MMA8652_ADDR, MMA8652_PL_STATUS_REG_ADDR, &reg_status, 1);      /* Get the Motion source and clears interrupt*/	

		accel_data.x_value = reg_status;
		accel_data.y_value = 0x00;
		accel_data.z_value = 0x00;
		current_mode = 0x08;
			
		data_ready = true;
		
		accel_event_handler(accel_data);
	}
	else
	{
		//Do nothing
	}
}

/* Accelerometer Module initialization. Pass connection information
 * structure pointer so that the accel handler can access the connection handle.
 */
void Accel_Init(ble_bmdeval_t * p_bmdeval)
{
	if(initilized	 == true)
	{
		return;
	}
	accel_bmdeval = p_bmdeval;
	
	if(!I2CInit())
	{
		while(1); //Accelerometer failed to initialize
	}
	//Initialize the Accelerometer
	MMA8652Init();
	
	initilized = true;
	
return;
}

/* Configure accelerometer into Data Ready mode. */
void Accel_Config_Data_Ready()
{
	MMA845EnableDataReadyMode();
	return;
}

/* Configure accelerometer into Orientation mode. */
void Accel_Config_Orientation()
{
	MMA8652EnableOrientationMode();
	return;
}

/* Configure accelerometer into Transient mode. */
void Accel_Config_Transient()
{
	MMA8652EnableVibrationMode();
	return;
}

/* Configure accelerometer into double-tap mode. */
void Accel_Config_Double_Tap()
{
	MMA845EnableDoubleTapMode();
	return;
}

/* Configure accelerometer into double-tap mode. */
void Accel_Config_Motion()
{
	MMA845EnableMotionMode();
	return;
}

/* Accelerometer module deinitialization function
 * NOTE: You MUST deinit the accelerometer when resetting the module,
 * otherwise a power cycle may be necessary to resume normal operation. 
*/
void Accel_Deinit()
{
    //Do not de-init if not initialized
	if(initilized == true)
    {
        //Deinitialize the Accelerometer
        MMA8652Deinit();
	
        //Negate the initialization flag
        initilized = false;
    }

	
return;
}

//Simple function for updating a single reading on the Accel. characteristic
void Accel_Data_Get()
{
	accel_event_handler(accel_data);
	
	return;
}

uint8_t Accel_Data_Get_Mode()
{
    return current_mode;
}

//Function for controling streaming flag
void Accel_Streaming_Mode(bool on_off)
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
