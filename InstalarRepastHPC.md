Os passos para fazer funcionar o HPC são os seguintes

1) Baixe o Repast HPC no link
http://sourceforge.net/projects/repast/files/Repast%20HPC/Repast%20HPC%201.0.1/repasthpc-1.0.1.tar.gz/download

2) Descompacte em uma pasta.

3) Usando o synaptic instale os seguintes pacotes:
> -libboost-all-dev
> - libnetcdf-dev
> - openmpi-bi

4) adicione  $(BOOST\_SYSTEM\_LIBS) à linha  zombie\_model\_LDADD no
arquivo src/zombie/Makefile.in

5) adicione  $(BOOST\_SYSTEM\_LIBS) à linha  rumor\_model\_LDADD no
arquivo  src/rumor/Makefile.in

6) no diretorio onde foi descompactado o repast digite os seguintes comandos
> ./configure
> make
> sudo make install

7) para testar execute o comando abaixo no diretorio examples\_bin/zombies

sudo mpirun -np 4 ./zombie\_model config.props model.props