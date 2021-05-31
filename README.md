# JavaSftp
Command line sftp application in Java. Made to transfer files from and to other servers. 

## Supported authentications
- username/password
- username/private_key

## Features
- test connection
- list directory (optionaly csv output)
- get file
- put file
- remove file
- proxy support
- bind interface (if multiple interface on server)


## Usage
```bash
# you can get a list of arguments by just calling the app from command line without arguments

$> java -jar JavaSftp.jar

usage: SFTP Manager 1.6 (added proxy)
 -a,--args                   Lsit args.
 -b,--put                    Put file.
 -d,--dir <arg>              Remote working directory.
 -f,--trg <arg>              Local file name.
 -g,--get                    Get file.
 -h,--host <arg>             Host
 -i,--help                   Help
 -k,--privateKeyFile <arg>   Private key location.
 -l,--listDir                List directory.
 -o,--csvOutput              Get nice csv output for list directory.
 -p,--port <arg>             Port
 -q,--password <arg>         Password
 -r,--remove                 Remove file.
 -s,--src <arg>              File name on sftp location.
 -t,--test                   Test connection.
 -u,--user <arg>             User
 -v,--verbose                Get loging from JSCH.
 -x,--proxyHost <arg>        Proxy host.
 -y,--proxyPort <arg>        Proxy port.
 -z,--proxyType <arg>        Proxy type. (ie. sockks5)
```

## Examples

```bash
# test connection
$> java -jar JavaSftp.jar  \
  --test                   \
  --host      host_ip      \
  --port      22           \
  --user      usr_name     \
  --password  pass_word
```
