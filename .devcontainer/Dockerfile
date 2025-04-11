FROM jetpackio/devbox:latest

# Installing your devbox project
WORKDIR /code
USER root:root

# Install procps for ps command and other process utilities
# in case it is needed from the IDEs uncomment the following line
# RUN apt-get update && apt-get install -y procps && apt-get clean

RUN mkdir -p /code && chown ${DEVBOX_USER}:${DEVBOX_USER} /code

USER ${DEVBOX_USER}:${DEVBOX_USER}
COPY --chown=${DEVBOX_USER}:${DEVBOX_USER} devbox.json devbox.json
COPY --chown=${DEVBOX_USER}:${DEVBOX_USER} devbox.lock devbox.lock



RUN devbox run -- echo "Installed Packages."

RUN devbox shellenv --init-hook >> ~/.profile
