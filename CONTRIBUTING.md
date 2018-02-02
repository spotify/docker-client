# Contributing


## Response Times

This project is developed and maintained by an infrastructure team at Spotify. Lots of teams at
Spotify use relatively recent versions of this project in production for mission-critical systems.

That being said, this is our day job where our primary users are our colleagues.
So we might be slow in getting back to you because we're busy working on Spotify-specific things
or because your issues are being prioritized behind those of our colleagues.

Please poke us if you feel you're being neglected, and we'll do our best to get back to you.

## Related Tools You May Find Useful

If you like this project, you might also like [dockerfile-maven][2], [helios][3], [docker-gc][4],
[helios-skydns][5], and [helios-consul][6].


## Reporting Bugs

Please make sure you're using the latest version. This project is
released continuously as it's developed so new releases come out almost as frequently as we
commit to master.

## Contributing

Before creating a new issue, see if there's already an existing issue.

If you create a minor bugfix, feel free to submit a PR.
If your PR is for a significant change or a new feature, feel free to ask for our feedback
before writing code to check we're on the same page.

You can build and test by following [instructions here][1].

### Unit tests and integration tests
When adding new functionality to DefaultDockerClient, please consider and
prioritize adding unit tests to cover the new functionality in
[DefaultDockerClientUnitTest][] rather than integration tests that require a
real docker daemon in [DefaultDockerClientTest][].

DefaultDockerClientUnitTest uses a [MockWebServer][] where we can control the
HTTP responses sent by the server and capture the HTTP requests sent by the
DefaultDockerClient, to ensure that it is communicating with the Docker Remote
API as expected.

While integration tests are valuable, they are more brittle and harder to run
than a simple unit test that captures/asserts HTTP requests and responses, and
they end up testing both how docker-client behaves and how the docker daemon
itself behaves.

  [1]: https://github.com/spotify/docker-client#testing
  [2]: https://github.com/spotify/dockerfile-maven
  [3]: https://github.com/spotify/helios
  [4]: https://github.com/spotify/docker-gc
  [5]: https://github.com/spotify/helios-skydns
  [6]: https://github.com/spotify/helios-consul
  [DefaultDockerClientTest]: src/test/java/com/spotify/docker/client/DefaultDockerClientTest.java
  [DefaultDockerClientUnitTest]: src/test/java/com/spotify/docker/client/DefaultDockerClientUnitTest.java
  [MockWebServer]: https://github.com/square/okhttp/tree/master/mockwebserver
