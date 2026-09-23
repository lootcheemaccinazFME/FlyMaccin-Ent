#include "chiaki_adapter.h"
int fme_chiaki_available(void) {
#ifdef FME_HAS_CHIAKI
  return 1;
#else
  return 0;
#endif
}
const char *fme_chiaki_stage(void) {
#ifdef FME_HAS_CHIAKI
  return "chiaki-core-linked";
#else
  return "chiaki-core-not-linked";
#endif
}
