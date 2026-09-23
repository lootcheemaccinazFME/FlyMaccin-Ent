#pragma once
#include <stddef.h>
#include <stdint.h>
#ifdef __cplusplus
extern "C" {
#endif
int fme_chiaki_available(void);
const char *fme_chiaki_stage(void);

typedef void (*FmeVideoSampleCallback)(const uint8_t *buf, size_t size, void *user);
typedef void (*FmeAudioFrameCallback)(const int16_t *samples, size_t frames, int channels, int sample_rate, void *user);

typedef struct {
  FmeVideoSampleCallback video_sample;
  FmeAudioFrameCallback audio_frame;
  void *user;
} FmeMediaCallbacks;

int fme_chiaki_set_media_callbacks(const FmeMediaCallbacks *callbacks);
int fme_chiaki_media_pipeline_ready(void);
#ifdef __cplusplus
}
#endif
