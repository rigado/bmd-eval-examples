#include "unity_fixture.h"

static void RunAllTests(void)
{
    RUN_TEST_GROUP(BmdAccel);
}

int32_t run_test_suite(void)
{
    char * argv[] = { "-v" };
    return UnityMain(1, (const char**)argv, RunAllTests);
}
