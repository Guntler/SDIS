Turma 6 e 4, Daniel Pereira (201109110) e Jo�o Marinheiro (201101774)

O programa deve ser inicializado compilando primeiro o ficheiro DistributedBackupSystem.java:

% javac -g DistributedBackupSystem.java

Ap�s a finaliza��o da compila��o, pode ser executado o programa com o seguinte formato:

% java DistributedBackupSystem <IPMC> <portMC> <ipMDB> <portMDB> <ipMDR> <portMDR>

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