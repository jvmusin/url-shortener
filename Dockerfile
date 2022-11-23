FROM ghcr.io/graalvm/native-image:latest
COPY . /app
WORKDIR /app
RUN ./gradlew stage

FROM ubuntu:latest
COPY --from=0 /app/build/native/nativeCompile/url-shortener app
CMD ["./app"]
