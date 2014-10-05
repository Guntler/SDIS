SDIS
====
A small application written for the Distributed Systems course at FEUP. It allows the user to backup files across several computers using multicast communication. Below are the instructions to run the program, in portuguese.

Compilation
===========
Turma 6 e 4, Daniel Pereira (201109110) e João Marinheiro (201101774)

Recomenda-se que o programa seja compilado através da ferramenta Eclipse importando a pasta src
presente nesta submissão para um novo projecto Java. Deve depois, com um clique direito no projecto,
escolher 'Export...', expandir a pasta Java e escolher 'Runnable JAR File'. Convém salientar que o directório
de java deve estar na variável de ambiente %PATH.
Depois de escolhido o nome e a localização do ficheiro JAR, deve aceder-se à pasta através da linha
de comandos e iniciar o programa com:

% java -jar *nome atribuído ao jar*.jar <IPMC> <portMC> <ipMDB> <portMDB> <ipMDR> <portMDR>

Para executar os protocolos do programa, é necessário fornecer ao mesmo as instruções necessárias,
já depois de ter iniciado o programa:

Protocolo de Backup:
backup "filename" repDegree - em que filename especifica e inclui o caminho completo para o ficheiro desejado,
				e repDegree o grau de replicação desejado

Protocolo de Restauro:
restore "filename" 	- em que filename especifica e inclui o caminho completo para o ficheiro desejado.

Protocolo de Deleção:
delete "filename"  	- em que filename especifica e inclui o caminho completo para o ficheiro desejado.

Protocolo de Reclamação de Espaço:
setAllocatedMemory numberOfBytes - em que numberOfBytes especifica o número de bytes alocados para armazenamento de dados.
