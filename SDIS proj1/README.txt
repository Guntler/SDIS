Turma 6 e 4, Daniel Pereira (201109110) e Jo�o Marinheiro (201101774)

Recomenda-se que o programa seja compilado atrav�s da ferramenta Eclipse importando a pasta src
presente nesta submiss�o para um novo projecto Java. Deve depois, com um clique direito no projecto,
escolher 'Export...', expandir a pasta Java e escolher 'Runnable JAR File'. Conv�m salientar que o direct�rio
de java deve estar na vari�vel de ambiente %PATH.
Depois de escolhido o nome e a localiza��o do ficheiro JAR, deve aceder-se � pasta atrav�s da linha
de comandos e iniciar o programa com:

% java -jar *nome atribu�do ao jar*.jar <IPMC> <portMC> <ipMDB> <portMDB> <ipMDR> <portMDR>

Para executar os protocolos do programa, � necess�rio fornecer ao mesmo as instru��es necess�rias,
j� depois de ter iniciado o programa:

Protocolo de Backup:
backup "filename" repDegree - em que filename especifica e inclui o caminho completo para o ficheiro desejado,
				e repDegree o grau de replica��o desejado

Protocolo de Restauro:
restore "filename" 	- em que filename especifica e inclui o caminho completo para o ficheiro desejado.

Protocolo de Dele��o:
delete "filename"  	- em que filename especifica e inclui o caminho completo para o ficheiro desejado.

Protocolo de Reclama��o de Espa�o:
setAllocatedMemory numberOfBytes - em que numberOfBytes especifica o n�mero de bytes alocados para armazenamento de dados.