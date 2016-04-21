
# SoundSword
SoundSword is an Android library which massively simplifies the task of playing audio in the background. The [PlaybackService](library/src/main/java/com/matthewtamlin/soundsword/PlaybackService.java) class of the library is designed to play any object which implements the [PlayableMedia](library/src/main/java/com/matthewtamlin/soundsword/PlayableMedia.java) interface. At the core of the service is the Android MediaPlayer API, but you no longer need to worry about nasty try-catch blocks or illegal state exceptions. The service tracks the internal state of the media player to make sure that only allowed operations are requested. If an operation which cannot be performed is requested, then the service will intercept the operation and prevent it from being executed.

To use this library, add `compile 'com.matthew-tamlin:sound-sword:1.0.1'` to your gradle build file.

## PlaybackService
The PlaybackService class is the primary class of this library. Since services operate independently of the activity lifecycle, instances of PlaybackService can be used to play media as the user navigates between activities or when the app is in the background. Binding to the service allows clients to control playback, and registering for callbacks allows clients to receive feedback. 

The service is controlled with two kinds of methods: Request operation methods and standard methods. The request operation methods return almost immediately as they simply queue operations for asynchronous processing. Operations are executed one at a time on the 'playback thread' to avoid blocking the UI. Operations are executed in the order in which they are requested, with the exception of the change media source operation. Requesting this operation terminates the current operation and cancels all pending operations. Whenever an operation finishes, the next operation in the queue is immediately executed until there are no more operations to execute. The operations which can be requested are:
- Change media source.
- Start/resume playback.
- Pause playback.
- Stop playback.
- Seek to a particular position in the media.

See the Javadoc of the request methods for a more in depth explanation of what each operation does. 

The standard methods allow the client to configure the service or query its properties. The standard methods do not perform any asynchronous operations and will run entirely on the calling thread. The standard methods allow the client to:
- Change the volume profile.
- Get the current playback position.
- Determine if the media is currently playing.
- Determine if an operation can be performed at the current time.
- Enable/disable looping.
- Configure the video output.
- Register for callbacks.
- Enable/disable automatic stopping of the service when playback completes.
- Terminate the current operation, clear pending operations, and terminate playback.

See the Javadoc of the standard methods for a more in depth explanation of what each method does. 

A PlaybackService can pass information back to the client by means of four separate callbacks. Callbacks are delivered one at a time on the 'callback thread', therefore it is important that the callback listeners do not perform UI related tasks without using the `Activity.runOnUiThread(Runnable)` method. The callbacks are:
- `OnOperationStartedListener.onOperationStarted(PlaybackService, Operation)` is invoked by the PlaybackService whenever an operation is started. The callback is passed a reference to the service that issued the callback, as well as an enum constant which identifies the operation.
- `OnOperationFinishedListener.onOperationFinishedListener(PlaybackService, Operation, FailureMode)` is invoked by the PlabackService whenever an operation finishes, either by successful completion or failure. The callback is passed a reference to the service that issued the callback, as well as an enum constant which identifies the operation and an enum constant which identifies the failure mode. The failure mode is `null` if the operation completed successfully.
- `OnPendingOperationsCancelledListener.onPendingOperationsCancelled(PlaybackService)` is invoked by the PlaybackService whenever the pending operation queue is cleared, regardless of whether or not the queue was empty. The callback is passed a reference to the PlaybackService which issued the callback.
- `OnPlaybackCompleteListener` is invoked by the PlaybackService whenever the end of the current media is reached. The callback may be called multiple times if looping has been enabled. The callback is passed a reference to the PlaybackService which issued the callback.
 
See the Javadoc of the callbacks for a more in depth explanation.

PlaybackService conforms to the Android guidelines regarding media playback. Playback will automatically stop whenever audio focus is lost, and playback will automatically pause whenever the system indicates that playback is "becoming noisy" (e.g. headphones have been removed). The service also listens for requests for transient audio ducking and changes the volume appropriately. These features cannot be disabled, however they can be customised by changing the volume profile.

## Attribution
This repo contains [music](testapp/src/main/assets) sourced from the [Bensound](http://www.bensound.com/royalty-free-music/electronica) royalty free music collection. The music is licensed by the creator under the 'Creative Commons - Attribution - No Derivative Works' license and is not covered by the [licensing terms](LICENSE) of this library.
