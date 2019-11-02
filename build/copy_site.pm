use Net::SFTP::Foreign;

$VERSION = `cat ./common/VERSION`;
$FTPDIR = "/var/ftp/pub/bgerp/$VERSION";
$JAVADOCS_DIR = "/home/www/www.bgerp.ru/javadoc";

#$VERSION_DIR = "$FTPROOT/$VERSION/";

# SSH access options
$SSH_HOST = "bgerp.ru";
$SSH_USER = "www";

sub bgcrm_connect()
{
	my $sftp = Net::SFTP::Foreign->new( host => $SSH_HOST, user => $SSH_USER );
	$sftp->error and die "Unable to connect to remote host: ".$sftp->error;
	return $sftp;
}

1;