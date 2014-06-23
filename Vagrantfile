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
	config.vm.define "virtualbox" do |vbox|
		vbox.vm.box = "puppetlabs/centos-6.5-64-puppet"

		vbox.vm.provision "shell", inline: $installPuppet

	    vbox.vm.synced_folder "./puppet/", "/puppet"

	    vbox.vm.provision "shell", inline: $addMongoRepo

	    vbox.vm.provision "shell", inline: $addTomcatRepo

	    vbox.vm.provision "shell",
    	    inline: "puppet apply /puppet/default.pp"

	    vbox.vm.network "forwarded_port", host: 4567, guest: 8080
	end

	config.vm.define "openstack", autostart: false do |osConfig|
		require 'vagrant-openstack-plugin'

		osConfig.ssh.private_key_path = "PathToPrivateKey.pem"
		
		osConfig.vm.box = "dummy"
		osConfig.vm.box_url = "https://github.com/cloudbau/vagrant-openstack-plugin/raw/master/dummy.box"

		osConfig.vm.provider :openstack do |os|
			os.username     = "#{ENV['OS_USERNAME']}"
    	    os.api_key      = "#{ENV['OS_PASSWORD']}"
        	os.flavor       = /m1.small/                # Regex or String
	        os.image        = /xdata-centos-base/                 # Regex or String
    	    os.endpoint     = "#{ENV['OS_AUTH_URL']}/tokens"
        	os.keypair_name = "KayPairName"      # as stored in Nova
	        os.ssh_username = "cloud-user"           # login for the VM
        	os.floating_ip = "auto"
        	os.networks = []
			#os.server_name = "A_name_for_the_ server"
		end

        osConfig.ssh.pty = true

        osConfig.vm.synced_folder ".", "/vagrant", disabled: true

		osConfig.vm.provision "shell", inline: $installPuppet

	    osConfig.vm.synced_folder "./puppet/", "/puppet"

	    osConfig.vm.provision "shell", inline: $addMongoRepo

    	osConfig.vm.provision "shell", inline: $addTomcatRepo

	    osConfig.vm.provision "shell",
    	    inline: "puppet apply /puppet/default.pp"

	    osConfig.vm.network "forwarded_port", host: 4567, guest: 8080
	end
end	
