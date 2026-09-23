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
// Linking chiaki-lib is not the same as having a working media pipeline.
  // Return ready only after a real ChiakiSession plus video/audio sinks are
  // constructed and wired to Android MediaCodec/AudioTrack.
  return 0;
}
