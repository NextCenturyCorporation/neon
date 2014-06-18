VAGRANTFILE_API_VERSION = "2"

$addMongoRepo = <<ADDMONGOREPO
	echo "Setting up mongo repo"
	cp /vagrant/puppet/yum/mongodb.repo /etc/yum.repos.d/mongodb.repo
	echo "Done adding mongo repo"
ADDMONGOREPO

$addTomcatRepo = <<ADDTOMCATREPO
	echo "Setting up tomcat repo"
	cp /vagrant/puppet/yum/jpackage.repo /etc/yum.repos.d/
	echo "Done adding tomcat repo"
ADDTOMCATREPO

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
	
	config.vm.provider :openstack do |os|
		require 'vagrant-openstack-plugin'
		
		#os.username     = "YOUR USERNAME"          # e.g. "#{ENV['OS_USERNAME']}"
		#os.api_key      = "YOUR API KEY"           # e.g. "#{ENV['OS_PASSWORD']}"
		#os.flavor       = /m1.tiny/                # Regex or String
		#os.image        = /Ubuntu/                 # Regex or String
		#os.endpoint     = "KEYSTONE AUTH URL"      # e.g. "#{ENV['OS_AUTH_URL']}/tokens"
		#os.keypair_name = "YOUR KEYPAIR NAME"      # as stored in Nova
		#os.ssh_username = "SSH USERNAME"           # login for the VM
	end

	config.vm.box = "puppetlabs/centos-6.5-64-puppet"

	config.vm.provision "shell", inline: $addMongoRepo

	config.vm.provision "shell", inline: $addTomcatRepo

	config.vm.provision "shell",
		inline: "puppet apply /vagrant/puppet/default.pp"	

	config.vm.network "forwarded_port", host: 4567, guest: 8080
  #config.vm.provision :puppet do |puppet|
  #  puppet.options = ['--verbose']
  #end

  # Create a forwarded port mapping which allows access to a specific port
  # within the machine from a port on the host machine. In the example below,
  # accessing "localhost:8080" will access port 80 on the guest machine.
  # config.vm.network "forwarded_port", guest: 80, host: 8080

  # Create a private network, which allows host-only access to the machine
  # using a specific IP.
  # config.vm.network "private_network", ip: "192.168.33.10"

  # Create a public network, which generally matched to bridged network.
  # Bridged networks make the machine appear as another physical device on
  # your network.
  # config.vm.network "public_network"

  # If true, then any SSH connections made will enable agent forwarding.
  # Default value: false
  # config.ssh.forward_agent = true
end
