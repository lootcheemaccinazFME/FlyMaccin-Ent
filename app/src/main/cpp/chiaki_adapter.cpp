#include "chiaki_adapter.h"
#include <mutex>

#ifdef FME_HAS_CHIAKI
#include <chiaki/session.h>
#include <chiaki/controller.h>
#include <chiaki/feedbacksender.h>
#endif

static std::mutex g_media_mutex;
static FmeMediaCallbacks g_media_callbacks = {};

int fme_chiaki_available(void) {
#ifdef FME_HAS_CHIAKI
  return 1;
#else
  return 0;
#endif
}

const char *fme_chiaki_stage(void) {
#ifdef FME_HAS_CHIAKI
  return "chiaki-core-linked-media-bridge";
#else
  return "chiaki-core-not-linked";
#endif
}

int fme_chiaki_set_media_callbacks(const FmeMediaCallbacks *callbacks) {
  std::lock_guard<std::mutex> lock(g_media_mutex);
  if(callbacks) g_media_callbacks = *callbacks;
  else g_media_callbacks = {};
  return 0;
}

int fme_chiaki_media_pipeline_ready(void) {
#ifdef FME_HAS_CHIAKI
  // session.h exposes the raw encoded video callback and the session/event
  // lifecycle used by the FME Android decoder bridge. Audio is intentionally
  // kept as a separate sink so Android AudioTrack can own playback.
  return 1;
#else
  return 0;
#endif
}
