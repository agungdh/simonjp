FROM gcr.io/distroless/cc-debian13:nonroot

WORKDIR /work/
COPY --chown=nonroot:nonroot --chmod=0755 build/native/nativeCompile/simonjp /work/application

ENV SERVER_PORT=8080

EXPOSE 8080
USER nonroot

ENTRYPOINT ["/work/application"]