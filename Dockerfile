FROM ghcr.io/graalvm/native-image:latest
COPY . /app
WORKDIR /app
RUN ./gradlew stage
ENTRYPOINT ["build/native/nativeCompile/url-shortener"]
CMD [""]
