# UI tests

[Maestro](https://maestro.dev) flows that run against the release APK, so R8 is
covered as well. They use the production server.

## Running

Start an emulator, install Maestro, then:

```sh
./gradlew assembleRelease
./scripts/run-ui-tests.sh                # all regular flows
./scripts/run-ui-tests.sh --public-chat  # also posts to the public chat
```

Results end up in `build/ui-tests`. Uninstall the debug build first: it can't be
installed next to the release build because both use the same `fileprovider` name.

## What reaches real users

| Flow | Effect |
|---|---|
| `position_published` | Publishes a rider in the open Atlantic for a few minutes |
| `manual/chat_send` | Posts a message to the public chat, deleted after 30 minutes |

`manual/chat_send` is not part of CI and only runs when started by hand.

## Rules for new flows

- Always pass `permissions` to `launchApp`. Maestro allows everything by default,
  and with location access the app starts publishing positions.
- Call `setLocation` before location access is granted, otherwise the emulator's
  default position in California can be published.
- Enable observer mode before granting location, unless the flow is about
  publishing a position.
- Only tap `chat_send_button` in `manual/`.
- Only tap the photo upload button after asserting that the device is offline.

## Known bugs

Flows in `known-bugs/` fail until the bug is fixed and are not part of the regular
run. Move them up one folder together with the fix.
