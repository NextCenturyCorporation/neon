VAGRANTFILE_API_VERSION = "2"

$addMongoRepo = <<ADDMONGOREPO
	echo "Setting up mongo repo"
	cp /puppet/yum/mongodb.repo /etc/yum.repos.d/mongodb.repo
	echo "Done adding mongo repo"
ADDMONGOREPO

$addTomcatRepo = <<ADDTOMCATREPO
	echo "Setting up tomcat repo"
	cp /puppet/yum/jpackage.repo /etc/yum.repos.d/
	echo "Done adding tomcat repo"
ADDTOMCATREPO

$installPuppet = <<INSTALLPUPPET
	echo "Installing puppet"
	yum -y install puppet	
	echo "Done installing puppet"
INSTALLPUPPET

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
	
	config.vm.box = "puppetlabs/centos-6.5-64-puppet"
	
	config.vm.provider "openstack" do |os|
		require 'vagrant-openstack-plugin'

		config.ssh.private_key_path = "~/.ssh/neonxdatakeypair-vagrant.pem"
	
		config.vm.box = "dummy"
	
		os.username     = "#{ENV['OS_USERNAME']}"
		os.api_key      = "#{ENV['OS_PASSWORD']}"
		os.flavor       = /m1.small/                # Regex or String
		os.image        = /xdata-centos-base/                 # Regex or String
		os.endpoint     = "#{ENV['OS_AUTH_URL']}/tokens"
		os.keypair_name = "KeyPairName"      # as stored in Nova
		os.ssh_username = "cloud-user"           # login for the VM
		os.networks = []
		os.floating_ip = "auto"
		config.ssh.pty = true

		config.vm.synced_folder ".", "/vagrant", disabled: true
	end

	config.vm.provision "shell", inline: $installPuppet
	
	config.vm.synced_folder "./puppet/", "/puppet"

	config.vm.provision "shell", inline: $addMongoRepo

	config.vm.provision "shell", inline: $addTomcatRepo

	config.vm.provision "shell",
		inline: "puppet apply /puppet/default.pp"	

	config.vm.network "forwarded_port", host: 4567, guest: 8080
end
