FROM gitpod/workspace-full-vnc

USER gitpod

RUN sudo apt-get update && \
    sudo apt-get install -yq chromium-browser

ENV CHROME_BIN=/usr/bin/chromium-browser
ENV BROWSER="ChromeHeadless"
