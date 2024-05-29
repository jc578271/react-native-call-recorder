
#ifdef RCT_NEW_ARCH_ENABLED
#import "RNCallRecorderSpec.h"

@interface CallRecorder : NSObject <NativeCallRecorderSpec>
#else
#import <React/RCTBridgeModule.h>

@interface CallRecorder : NSObject <RCTBridgeModule>
#endif

@end
