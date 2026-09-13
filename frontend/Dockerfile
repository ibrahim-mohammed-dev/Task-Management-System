# Stage 1: Build
FROM node:22-alpine AS builder

# Set the working directory inside the container
WORKDIR /app

# Copy only dependency files first (better layer caching)
COPY package.json package-lock.json ./

# Install all dependencies (including devDependencies needed to build)
RUN npm ci

# Copy the rest of the source code
COPY . .

# Build the production-ready static files into /app/dist
RUN npm run build

# Stage 2: Serve
FROM nginx:stable-alpine AS runner

# Remove the default nginx welcome page
RUN rm -rf /usr/share/nginx/html/*

# Copy the built files from the builder stage into nginx serving directory
COPY --from=builder /app/dist /usr/share/nginx/html

# Expose port 80 so Docker (and docker-compose) can map it
EXPOSE 80

# Start nginx in the foreground (required for containers)
CMD ["nginx", "-g", "daemon off;"]
