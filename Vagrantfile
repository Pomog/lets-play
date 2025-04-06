# -- mode: ruby --
# vi: set ft=ruby :

# All Vagrant configuration is done below.
Vagrant.configure("2") do |config|
  # Use the official Ubuntu 20.04 LTS box
  config.vm.box = "ubuntu/focal64"

  # Create a private network with a fixed IP (useful for host-only access)
  config.vm.network "private_network", ip: "192.168.56.82"

  # Create a public network (bridged network) so the VM appears as a physical device on your LAN
  config.vm.network "public_network"

  # Provider-specific configuration for VirtualBox (e.g., memory allocation)
  config.vm.provider "virtualbox" do |vb|
    vb.memory = "2048"
  end

  # Provisioning: install Docker from Docker's official repository, run a MongoDB container,
  # and create an initial MongoDB database called 'lets-play'
  config.vm.provision "shell", inline: <<-SHELL
    # Update package list and install prerequisites
    sudo apt-get update
    sudo apt-get install -y ca-certificates curl gnupg

    # Create directory for Docker GPG keys and add Docker's official GPG key
    sudo install -m 0755 -d /etc/apt/keyrings
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
    sudo chmod a+r /etc/apt/keyrings/docker.gpg

    # Add Docker's repository to Apt sources
    echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo \"$VERSION_CODENAME\") stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

    # Update package list with new Docker repository
    sudo apt-get update

    # Install Docker Engine, CLI, containerd, and Docker Compose plugins
    sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

    # Download Docker Compose binary (version 2.1.1 in this example) and set execute permissions
    sudo curl -L "https://github.com/docker/compose/releases/download/v2.1.1/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    sudo chmod +x /usr/local/bin/docker-compose

    # Remove any existing MongoDB container, if it exists
    sudo docker rm -f mongodb || true

    # Run the MongoDB container with port 27017 forwarded so it is accessible from other computers on the network
    sudo docker run -d --name mongodb -p 27017:27017 mongo:latest

    # Wait a few seconds to ensure MongoDB is ready to accept connections
    sleep 5

    # Create an initial database 'lets-play' by creating a dummy collection.
    # In MongoDB, creating a collection automatically creates the database if it doesn't exist.
    sudo docker exec mongodb mongosh --eval "db.getSiblingDB('lets-play').createCollection('init_collection')"
  SHELL
end
