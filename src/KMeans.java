import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

public class KMeans {

	/**
	 * matriz de dados que o k-means receber�, cada linha um documento, cada
	 * coluna uma palavra
	 */
	private int[][] dados;

	/**
	 * quantidade k de prototipos passada
	 */
	private int k;
	
	/**
	 * matriz com os prot�tipos int[k][numeroDimensoes];
	 */
	private int[][] prototipos;
	
	/**
	 * matriz com a similaridade cosseno entre os documentos e os prot�tipos
	 */
	private double[][] similaridadeCosseno;

	/**
	 * matriz binaria de parti��o int[k][numeroLinhas]
	 */
	private int[][] matrizParticao;
	
	/**
	 * quantiza��o do erro de agrupamento
	 */
	private double jcm;

	/**
	 * equivale ao numero de documentos
	 */
	private int numeroLinhas;
	
	/**
	 * equivale ao numero de palavras analisadas nos documentos
	 */
	private int numeroDimensoes;

	/**
	 * valores m�ximos dos n�meros que podem ser escolhidos aleatoriamente para a
	 * cria��o dos prototipos
	 */
	private int[] max;


	public KMeans(String arquivo, int k, int linhas, int colunas) {
		try {
			BufferedReader leitor = new BufferedReader(new FileReader(arquivo));

			/*
			 * Para popular a matriz dados � necess�rio saber o n�mero de linhas
			 * e o n�mero de dimens�es do corpus recebido
			 */
			numeroLinhas = linhas;
			numeroDimensoes = colunas;
			jcm = 0;
			this.k = k;


			/*
			 * Inicializa��o das matrizes
			 */
			dados = new int[numeroLinhas][numeroDimensoes];
			prototipos = new int[k][numeroDimensoes];
			similaridadeCosseno = new double[numeroLinhas][k];
			matrizParticao = new int[k][numeroLinhas];

			/*
			 *  Leitura do arquivo e popula��o da matriz de dados
			 */
			max = new int[numeroDimensoes];
			String linha = null;
			String[] numColunas = null;
			int i = 0;
			int j = 0;
			/*
			 * L� a linha que cont�m o nome das palavras
			 */
			leitor.readLine();
			
			
			while ((linha = leitor.readLine()) != null) {
				numColunas = linha.split(",");
				for(int co = 1; co <= colunas; co++) {
					dados[i][j] = Integer.parseInt(numColunas[co]);
					/*
					 * Os valores m�ximos j� ser�o descobertos simultaneamente a popula��o
					 */
					if (dados[i][j] > max[j]) {
						max[j] = dados[i][j];
					}
					j++;
				}
				j = 0;
				i++;
			}
			
			leitor.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * inicializa aleatoriamente os prototipos na primeira itera��o
	 * considerando o valor m�ximo de cada dimens�o
	 * @return
	 */
	public int[][] inicializarPrototipos() {
		int randomNumber = 0;
		Random rand = new Random();
		for(int prototipo = 0; prototipo < k; prototipo++){
			for(int palavra = 0; palavra < numeroDimensoes; palavra++){
				randomNumber = rand.nextInt(max[palavra] + 1);
				System.out.println(randomNumber + "  " + max[palavra]);
				prototipos[prototipo][palavra] = randomNumber;
			}
		}
		return prototipos;
	}
	
	/**
	 *  Define a similaridade cosseno entre todos os documentos com todos os prototipos
	 */
	public void definirSimilaridadeCosseno()
	{
		int atual = 0;
		double similaridade = 0;
		double somaXY = 0;
		double somaX = 0;
		double somaY = 0;
		
		/*
		 *  percorrendo prototipos
		 */
		for(int j = 0; j< k ; j++)
		{
			/*
			 * percorrendo documentos
			 * todos para cada prototipo
			 */
			for(int i = 0; i< numeroLinhas; i++)
			{
				/*
				 *  calculo da distancia para um documento
				 */
				while(atual<numeroDimensoes)
				{
					somaXY = somaXY + dados[i][atual] + prototipos[j][atual];
					somaX = somaX + (dados[i][atual]*dados[i][atual]);
					somaY = somaY + (prototipos[j][atual]*prototipos[j][atual]);
					atual++;
				}
				
				similaridade = somaXY/(Math.sqrt(somaX)*Math.sqrt(somaY));
				/*
				 * O valor de similaridade � retirado de 1 para o met�do de 
				 * agrupamento conseguir continuar considerando os maiores valores
				 * como mais distantes
				 */
				similaridadeCosseno[i][j] = 1 - similaridade;
				
				/*
				 *  atualizar todas as vari�veis para o pr�ximo vetor que ser� comparado com o prot�tipo atual
				 */
				atual = 0;
				somaXY = 0;
				somaX = 0;
				somaY = 0;
			}
		}
	}
	
	public void clustering() {
		inicializarMatrizParticao();
		double menorDistancia = 0;
		double atual = 0;
		int cluster = 0;
		for (int documento = 0; documento < numeroLinhas; documento++) {
			/*
			 *  Calcula qual o prototipo com menor distancia e assim define um
			 *  cluster para o dado
			 */
			for (int prototipo = 0; prototipo < k; prototipo++) {
				atual = similaridadeCosseno[documento][prototipo];
				if (prototipo == 0) {
					menorDistancia = atual;
					cluster = prototipo;
				}
				if (atual < menorDistancia) {
					menorDistancia = atual;
					cluster = prototipo;
				}
			}
			/*
			 *  adiciona o valor um a matriz de particao no local marcando o
			 *  cluster a qual pertence
			 */
			matrizParticao[cluster][documento] = 1;
		}

	}
	
	/**
	 * zera matriz de parti��o
	 */
	private void inicializarMatrizParticao() {

		for (int i = 0; i < k; i++) {
			for (int j = 0; j < numeroLinhas; j++) {
				matrizParticao[i][j] = 0;
			}
		}
	}

	public double calcularJCM() {
		double jcmAtual = 0;
		for (int prototipo = 0; prototipo < k; prototipo++) {
			for (int documento = 0; documento < numeroLinhas; documento++) {

				jcmAtual = jcmAtual
						+ (matrizParticao[prototipo][documento] * (similaridadeCosseno[documento][prototipo] * similaridadeCosseno[documento][prototipo]));
			}
		}
		return jcmAtual;
	}
	
	public int [][] redefinirPrototipos() {
		int integrantes = 0; 
		for (int prototipo = 0; prototipo < k; prototipo++) {
			for (int documento = 0; documento < numeroLinhas; documento++) {
				if (matrizParticao[prototipo][documento] == 1) {
					if (integrantes == 0){
						inicializarPrototipo(prototipo);
					}
					integrantes++;
					for(int palavra = 0; palavra < numeroDimensoes; palavra++){
						prototipos[prototipo][palavra] = prototipos[prototipo][palavra] + dados[documento][palavra];
					}
					
				}
				
			}
			/*
			 *  evitar erro aritim�tico de divis�o por 0				
			 */
			if(integrantes>0){
				for (int palavra = 0; palavra < numeroDimensoes; palavra++) {
					prototipos[prototipo][palavra] = (prototipos[prototipo][palavra]/integrantes);
				}
			}
			integrantes = 0;
		}
		return prototipos;
	}

	/** 
	 * zera matriz de prototipos
	 * @param prototipo
	 */
	private void inicializarPrototipo(int prototipo) {
		for (int j = 0; j < numeroDimensoes; j++) {
			prototipos[prototipo][j] = 0;
		}
	}
	
	/** 
	 * similaridade cosseno entre os prototipos da itera��o anterior com
	 * os da itera��o atual para saber a movimenta��o que ocorreu
	 * @param prototiposAnterior
	 * @return
	 */
	public double diferencaPrototipos(int [][] prototiposAnterior) {
		double resposta = 0;
		double similaridade = 0;
		double somaXY = 0;
		double somaX = 0;
		double somaY = 0;
		for(int prototipo = 0; prototipo<k; prototipo++){
			for(int palavra = 0; palavra<numeroDimensoes; palavra++){
				somaXY = somaXY + prototipos[prototipo][palavra] + prototiposAnterior[prototipo][palavra];
				somaX = somaX + (prototipos[prototipo][palavra]*prototipos[prototipo][palavra]);
				somaY = somaY + (prototiposAnterior[prototipo][palavra]*prototiposAnterior[prototipo][palavra]);
			}
			resposta = resposta + (1 - (somaXY/(Math.sqrt(somaX)*Math.sqrt(somaY))));
			
			/*
			 * atualizar vari�veis para o pr�ximo prot�tipo
			 */
			somaXY = 0;
			somaX = 0;
			somaY = 0;
		}
		resposta = resposta/k;
		return resposta;
	}


	public int[][] getPrototipos() {
		return this.prototipos;
	}


	public int[][] getMatrizParticao() {
		return matrizParticao;
	}


	
	
	
}
