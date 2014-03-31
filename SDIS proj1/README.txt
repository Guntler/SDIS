Turma 6 e 4, Daniel Pereira (201109110) e João Marinheiro (201101774)

O programa deve ser inicializado compilando primeiro o ficheiro DistributedBackupSystem.java:

% javac -g DistributedBackupSystem.java

Após a finalização da compilação, pode ser executado o programa com o seguinte formato:

% java DistributedBackupSystem <IPMC> <portMC> <ipMDB> <portMDB> <ipMDR> <portMDR>

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