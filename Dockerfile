FROM ghcr.io/graalvm/jdk:ol8-java17
RUN ./gradlew stage
CMD ["./build/native/nativeCompile/url-shortener"]
