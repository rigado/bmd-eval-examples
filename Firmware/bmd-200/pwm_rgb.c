/** @file
 * 
 * @brief PWM module for use with the BMD-200 eval board.
 *
 * @details This module implements the PWM on the BMD-200 module and provides an
 * example on implementing PWM for the RGB LED on on the BMD-200 EVAL Board.
 * This file contains the code necessary to independently control pwm output on three separate channnels
 * given a 3 byte standard hex color value.
 */

#include <nrf.h>
#include <nrf_soc.h>

#include "nrf_gpio.h"
#include "nrf_gpiote.h"
#include "pwm_rgb.h"
#include "app_timer.h"
#include "boards.h"

/**
 * @defgroup rgb_pwm RGB LED PWM Module
 * @{
 * @ingroup rgb_pwm
 * @brief All items related to RGB LED PWM module.
 */

#define PWM_OUTPUT_PIN_NUMBER_1     BSP_LED_0							/**< For BMD-200 This is the Red LED.*/
#define PWM_OUTPUT_PIN_NUMBER_2     BSP_LED_1							/**< For BMD-200 This is the Green LED.*/
#define PWM_OUTPUT_PIN_NUMBER_3     BSP_LED_2							/**< For BMD-200 This is the Blue LED.*/
#define PWM_MAX_VALUE           	0xFF								/**< Maximum number of sample levels. Larger numbers will result in larger PWM period */
#define PWM_MIN_VALUE           	0x0003      						/**< Minimum number of sample levels. Values lower than this will result in no PWM output*/
#define PWM_TIMER_PRESCALER     	4          							/**< Prescaler setting for timer. 
																		Dictated by fTimer = 16 Mhz/ (2^PRESCALER). */
#define PERCENT_TO_PWM				(int)PWM_MAX_VALUE/100		        /**<multiplyer to get from percent to PWM value. */

#define PWM_TIMER               	NRF_TIMER2							/**< Which timer is to be used for pwm. */

/* PWM drive parameters */
static uint16_t         red_DutyCycle = 0;          /* red pwm duty cycle */
static uint16_t         green_DutyCycle = 0;        /* green pwm duty cycle */
static uint16_t         blue_DutyCycle = 0;         /* blue pwm duty cycle */
static bool				s_TimerNeededFlag = 1;      /* is PWM initialized */	
static ble_bmdeval_t    pwm_bmdeval;
/** @} */
/* initialization helpers */
static void pwm_deinit(void);
static void pwm_init(void);
static void timer_init(void);
static void timer_deinit(void);
static void gpiote_init(void);
static void gpiote_deinit(void);
static void ppi_init(void);
static void ppi_deinit(void);


void PWM_RGB_Init(ble_bmdeval_t * p_bmdeval)
{
    pwm_bmdeval = * p_bmdeval;
	red_DutyCycle = 0;          /* red pwm duty cycle */
    green_DutyCycle = 0;        /* green pwm duty cycle */
	blue_DutyCycle = 0;			/* blue pwm duty cycle */
}

void PWM_RGB_Start(ble_bmdeval_rgb_t * rgb_data, ble_bmdeval_t * p_bmdeval)
{
    PWM_RGB_Stop();
    pwm_bmdeval = * p_bmdeval;
    red_DutyCycle   = rgb_data->red_value * PERCENT_TO_PWM;
	green_DutyCycle = rgb_data->green_value * PERCENT_TO_PWM;
	blue_DutyCycle  = rgb_data->blue_value * PERCENT_TO_PWM;
    /* call pwm_init() to setup the timer->cc[1] regardless of if we are actually using the timer */
    pwm_init();        
	
    if(red_DutyCycle == PWM_MAX_VALUE)
    {
        /* don't use the timer it it isnt necessary */
        NRF_GPIO->OUTSET = (1<<PWM_OUTPUT_PIN_NUMBER_1);
    }
	else
        s_TimerNeededFlag = 1;
		
	if(green_DutyCycle == PWM_MAX_VALUE)
    {
        /* don't use the timer */
        NRF_GPIO->OUTSET = (1<<PWM_OUTPUT_PIN_NUMBER_2);
    }
	else
		s_TimerNeededFlag = 1;

	if(blue_DutyCycle == PWM_MAX_VALUE)
    {
        /* don't use the timer */
        NRF_GPIO->OUTSET = (1<<PWM_OUTPUT_PIN_NUMBER_3);
    }
	else
        s_TimerNeededFlag = 1;
		
    /* start pwm timer. Dont use the timer task if not necessary. */
	if(s_TimerNeededFlag == 1)
	{
        PWM_TIMER->TASKS_START = 1;
		s_TimerNeededFlag = 0;
			
        if(red_DutyCycle >= PWM_MIN_VALUE && red_DutyCycle != PWM_MAX_VALUE)
        {
            nrf_gpiote_task_enable(0);
        }
        if(green_DutyCycle >= PWM_MIN_VALUE && green_DutyCycle != PWM_MAX_VALUE)
        {
        nrf_gpiote_task_enable(1);
        }
        if(blue_DutyCycle >= PWM_MIN_VALUE && blue_DutyCycle != PWM_MAX_VALUE)
        {
            nrf_gpiote_task_enable(2);
        }
	}
}

void PWM_RGB_Stop(void)
{ 
    ble_bmdeval_on_pwm_change(&pwm_bmdeval, (ble_bmdeval_rgb_t){0,0,0});
    pwm_deinit();
}

/** @brief Function for initializing the Timer peripheral.
 */
static void timer_init(void)
{
    PWM_TIMER->MODE        = TIMER_MODE_MODE_Timer;
    PWM_TIMER->BITMODE     = TIMER_BITMODE_BITMODE_16Bit << TIMER_BITMODE_BITMODE_Pos;
    PWM_TIMER->PRESCALER   = PWM_TIMER_PRESCALER;

    /* stop & reset counter */
    PWM_TIMER->TASKS_STOP = 1;
    PWM_TIMER->TASKS_CLEAR = 1;
    
    PWM_TIMER->EVENTS_COMPARE[0] = 0;
    PWM_TIMER->EVENTS_COMPARE[1] = 0;
	PWM_TIMER->EVENTS_COMPARE[2] = 0;
	PWM_TIMER->EVENTS_COMPARE[3] = 0;
    
    /* load CC registers */
    PWM_TIMER->CC[0] = red_DutyCycle;
	PWM_TIMER->CC[1] = green_DutyCycle;
	PWM_TIMER->CC[2] = blue_DutyCycle;
    PWM_TIMER->CC[3] = PWM_MAX_VALUE;
    
    /* disable interrupts */
    PWM_TIMER->INTENCLR = 0xffffffff;
}

static void timer_deinit(void)
{
    /* stop the timer */
    PWM_TIMER->TASKS_STOP = 1;
    
    /* clear CC */
    PWM_TIMER->CC[0] = 0;
    PWM_TIMER->CC[1] = 0;
	PWM_TIMER->CC[2] = 0;
	PWM_TIMER->CC[3] = 0;
    
    /* disable interrupts */
    PWM_TIMER->INTENCLR = 0xffffffff;
}
    

/** @brief Function for initializing the GPIO Tasks/Events peripheral.
 */
static void gpiote_init(void)
{
    // Connect GPIO input buffers and configure PWM_OUTPUT_PIN_NUMBER as an output.
    nrf_gpio_cfg_output(PWM_OUTPUT_PIN_NUMBER_1);
	nrf_gpio_cfg_output(PWM_OUTPUT_PIN_NUMBER_2);
	nrf_gpio_cfg_output(PWM_OUTPUT_PIN_NUMBER_3);

    // Configure GPIOTE channels 0-2 to toggle the PWM pin state
    // @note Only one GPIOTE task can be connected to an output pin
    nrf_gpiote_task_configure(0, PWM_OUTPUT_PIN_NUMBER_1, NRF_GPIOTE_POLARITY_TOGGLE, NRF_GPIOTE_INITIAL_VALUE_HIGH);
	nrf_gpiote_task_configure(1, PWM_OUTPUT_PIN_NUMBER_2, NRF_GPIOTE_POLARITY_TOGGLE, NRF_GPIOTE_INITIAL_VALUE_HIGH);
	nrf_gpiote_task_configure(2, PWM_OUTPUT_PIN_NUMBER_3, NRF_GPIOTE_POLARITY_TOGGLE, NRF_GPIOTE_INITIAL_VALUE_HIGH);
}

static void gpiote_deinit(void)
{
	nrf_gpiote_task_disable(0);
	nrf_gpiote_task_disable(1);
	nrf_gpiote_task_disable(2);
    
    /* turn off */
    nrf_gpio_cfg_output(PWM_OUTPUT_PIN_NUMBER_1);
    NRF_GPIO->OUTCLR = (1<<PWM_OUTPUT_PIN_NUMBER_1);
    nrf_gpio_cfg_output(PWM_OUTPUT_PIN_NUMBER_2);
    NRF_GPIO->OUTCLR = (1<<PWM_OUTPUT_PIN_NUMBER_2);
    nrf_gpio_cfg_output(PWM_OUTPUT_PIN_NUMBER_3);
    NRF_GPIO->OUTCLR = (1<<PWM_OUTPUT_PIN_NUMBER_3);
}


/** @brief Function for initializing the Programmable Peripheral Interconnect peripheral.
 */
static void ppi_init(void)
{
/* connect timer CC to pin toggle task, timer clear */
    sd_ppi_channel_assign(0,&PWM_TIMER->EVENTS_COMPARE[0], &NRF_GPIOTE->TASKS_OUT[0]);
    sd_ppi_channel_assign(1,&PWM_TIMER->EVENTS_COMPARE[3], &NRF_GPIOTE->TASKS_OUT[0]);
    sd_ppi_channel_assign(2,&PWM_TIMER->EVENTS_COMPARE[3], &PWM_TIMER->TASKS_CLEAR);
    sd_ppi_channel_enable_set(PPI_CHEN_CH0_Msk|PPI_CHEN_CH1_Msk|PPI_CHEN_CH2_Msk);
/* connect timer CC to pin toggle task, timer clear */
    sd_ppi_channel_assign(3,&PWM_TIMER->EVENTS_COMPARE[1], &NRF_GPIOTE->TASKS_OUT[1]);
    sd_ppi_channel_assign(4,&PWM_TIMER->EVENTS_COMPARE[3], &NRF_GPIOTE->TASKS_OUT[1]);
    sd_ppi_channel_assign(5,&PWM_TIMER->EVENTS_COMPARE[3], &PWM_TIMER->TASKS_CLEAR);
    sd_ppi_channel_enable_set(PPI_CHEN_CH3_Msk|PPI_CHEN_CH4_Msk|PPI_CHEN_CH5_Msk);
/* connect timer CC to pin toggle task, timer clear */
    sd_ppi_channel_assign(6,&PWM_TIMER->EVENTS_COMPARE[2], &NRF_GPIOTE->TASKS_OUT[2]);
    sd_ppi_channel_assign(7,&PWM_TIMER->EVENTS_COMPARE[3], &NRF_GPIOTE->TASKS_OUT[2]);
    sd_ppi_channel_assign(8,&PWM_TIMER->EVENTS_COMPARE[3], &PWM_TIMER->TASKS_CLEAR);
    sd_ppi_channel_enable_set(PPI_CHEN_CH6_Msk|PPI_CHEN_CH7_Msk|PPI_CHEN_CH8_Msk);
}

static void ppi_deinit(void)
{
    sd_ppi_channel_enable_clr(PPI_CHEN_CH0_Msk|PPI_CHEN_CH1_Msk|PPI_CHEN_CH2_Msk);
	sd_ppi_channel_enable_clr(PPI_CHEN_CH3_Msk|PPI_CHEN_CH4_Msk|PPI_CHEN_CH5_Msk);
	sd_ppi_channel_enable_clr(PPI_CHEN_CH6_Msk|PPI_CHEN_CH7_Msk|PPI_CHEN_CH8_Msk);
}

static void pwm_deinit(void)
{
    red_DutyCycle = 0;
	green_DutyCycle = 0;
	blue_DutyCycle = 0;
    
    timer_deinit();
    ppi_deinit();
    gpiote_deinit();
}

static void pwm_init(void)
{
    timer_init();
    ppi_init();
    gpiote_init();
}

/** Turn off the PWM */
void pwm_off_timeout_handler(void * p_context)
{
    PWM_RGB_Stop();
}
