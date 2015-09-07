Guia passo a passo para a instalação do Repast 2 no Linux

# Detalhes #

1) Baixe o Eclipse 4.2 Classic <br>
2) No Help->Install new software selecione o repositorio Juno -<br>
<a href='http://download.eclipse.org/releases/juno'>http://download.eclipse.org/releases/juno</a> e selecione Programming<br>
languages -> Eclipse XML editors and tools (instale e reinicie) <br>
3) No Help->Install new software  digite o repositorio<br>
<a href='http://dist.springsource.org/release/GRECLIPSE/e4.2/'>http://dist.springsource.org/release/GRECLIPSE/e4.2/</a>  e selecione<br>
Groovy-Eclipse e extra groovy compilers  (instale e reinicie)<br>
4)  No Help->Install new software  digite o repositorio<br>
<a href='http://mirror.anl.gov/pub/repastsimphony'>http://mirror.anl.gov/pub/repastsimphony</a>  e selecione tudo  (instale e<br>
reinicie)<br>
5) Baixe do site<br>
<a href='http://download.java.net/media/jogl/builds/archive/jsr-231-1.1.1a/'>http://download.java.net/media/jogl/builds/archive/jsr-231-1.1.1a/</a>  a<br>
versão adequada para sua máquina no JOGL <br>
6) descompacte em uma pasta qualquer. <br>
7) Mova os arquivos "so" para /usr/lib <br>
8) Nos projetos que usem JOGL adicionem os jars jogl.jar e<br>
gluegen-rt.jar no CLASSPATH do Run Configurations -> aba Classpath -><br>
user entries <br>
9) Nos projetos que usem JOGL adicionem os jars jogl.jar e<br>
gluegen-rt.jar no build path -> add external jars <br>