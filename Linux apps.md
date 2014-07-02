sudo add-apt-repository ppa:git-core/ppa -y
sudo add-apt-repository ppa:eugenesan/ppa -y
sudo add-apt-repository ppa:webupd8team/java -y
sudo add-apt-repository ppa:webupd8team/atom -y
sudo add-apt-repository ppa:chris-lea/node.js -y
sudo add-apt-repository ppa:webupd8team/sublime-text-3 -y
sudo add-apt-repository ppa:paolorotolo/android-studio -y
sudo add-apt-repository "deb http://packages.mate-desktop.org/repo/ubuntu precise main" -y
sudo apt-get update
sudo apt-get --yes --quiet --allow-unauthenticated install mate-archive-keyring
sudo apt-get update
sudo apt-get install build-essential checkinstall ia32-libs
sudo apt-get install git git-cola smartgithg
sudo apt-get install mate-core mate-desktop-environment
sudo apt-get install oracle-java7-installer android-studio
sudo apt-get install nodejs sublime-text-installer
sudo apt-get install atom