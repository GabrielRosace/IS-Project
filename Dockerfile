FROM node:10.14-jessie

# USER "node"
COPY . /app
WORKDIR /app
EXPOSE 3000
EXPOSE 8080

RUN ["/bin/bash"]