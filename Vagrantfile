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

	config.vm.define "aws", autostart: false do |aws|   #install on ubuntu image
		require 'vagrant-aws'

		aws.vm.box = "dummy"
		aws.vm.box_url = "https://github.com/mitchellh/vagrant-aws/raw/master/dummy.box"

		aws.ssh.username = "ubuntu"
		aws.ssh.private_key_path = "PATH_TO_YOUR_KEY_FILE.pem"

		aws.vm.provider :aws do |ec2, override|
			ec2.access_key_id = "YOUR_ACCESS_KEY"
			ec2.secret_access_key = "YOUR_SECRET"
			ec2.keypair_name = "KEYPAIR_NAME"
			ec2.instance_type = "m1.medium"
			ec2.region = "us-east-1"
			ec2.security_groups = ["neon-vagrant"]
			ec2.tags = {
				'project' => 'neon'
			}
			ec2.block_device_mapping = [{ :DeviceName => "/dev/sda1", 'Ebs.VolumeSize' => 20, 'Ebs.DeleteOnTermination' => true }]
			ec2.ami = "ami-d805f4b0" #"ami-018c9568" #the ami for the image to install
			override.ssh.pty = true
		end
        
		aws.vm.synced_folder ".", "/vagrant", disabled: true

	    aws.vm.synced_folder "./puppet/", "/puppet"

	    aws.vm.provision "shell",
    	    inline: "echo \"Applying puppet\" &&  puppet apply /puppet/ubuntu.pp"

	    aws.vm.network "forwarded_port", host: 4567, guest: 8080
	end

	config.vm.define "aws-centos", autostart: false do |aws|
		require 'vagrant-aws'

		aws.vm.box = "dummy"
		aws.vm.box_url = "https://github.com/mitchellh/vagrant-aws/raw/master/dummy.box"

		aws.ssh.username = "root"
		aws.ssh.private_key_path = ""

		aws.vm.provider :aws do |ec2, override|
			ec2.access_key_id = "YOUR ACCESS ID"
			ec2.secret_access_key = "YOUR SECRET KEY"
			ec2.keypair_name = ""
			ec2.instance_type = "m1.medium"
			ec2.region = "us-east-1"
			ec2.security_groups = ["neon-vagrant"]
			ec2.tags = {
				'project' => 'neon'
			}
			ec2.block_device_mapping = [{ :DeviceName => "/dev/sda1", 'Ebs.VolumeSize' => 20, 'Ebs.DeleteOnTermination' => true }]
			ec2.ami = "ami-0d79c864" #"ami-8997afe0" #the ami for the image to install
			override.ssh.pty = true
		end
        
        aws.ssh.pty = true
		
		aws.vm.synced_folder ".", "/vagrant", disabled: true

		aws.vm.provision "shell", inline: $installPuppet

	    aws.vm.synced_folder "./puppet/", "/puppet"

	    aws.vm.provision "shell", inline: $addMongoRepo

    	aws.vm.provision "shell", inline: $addTomcatRepo

	    aws.vm.provision "shell",
    	    inline: "puppet apply /puppet/default.pp"

	    aws.vm.network "forwarded_port", host: 4567, guest: 8080
	end

	config.vm.define "openstack", autostart: false do |osConfig|
		require 'vagrant-openstack-plugin'

		osConfig.ssh.private_key_path = "/home/halfs13/.ssh/neonxdatakeypair-vagrant.pem"
		
		osConfig.vm.box = "dummy"
		osConfig.vm.box_url = "https://github.com/cloudbau/vagrant-openstack-plugin/raw/master/dummy.box"

		osConfig.vm.provider :openstack do |os|
			os.username     = "#{ENV['OS_USERNAME']}"
    	    os.api_key      = "#{ENV['OS_PASSWORD']}"
        	os.flavor       = /m1.small/                # Regex or String
	        os.image        = /xdata-centos-base/                 # Regex or String
    	    os.endpoint     = "#{ENV['OS_AUTH_URL']}/tokens"
        	os.keypair_name = "NeonXdataKeyPair-vagrant"      # as stored in Nova
	        os.ssh_username = "cloud-user"           # login for the VM
        	os.floating_ip = "auto"
        	os.networks = []
			os.server_name = "testing_snapshot"
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
