package org.fisco.bcos.asset.client;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.fisco.bcos.asset.contract.Asset;

import org.fisco.bcos.asset.contract.BuySomething;

import org.fisco.bcos.asset.contract.Asset.RegisterEventEventResponse;
import org.fisco.bcos.asset.contract.Asset.TransferEventEventResponse;
import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.Keys;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.channel.ChannelEthereumService;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.tuples.generated.Tuple2;
import org.fisco.bcos.web3j.tx.gas.StaticGasProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.fisco.bcos.web3j.tuples.generated.Tuple3;

public class AssetClient {

	static Logger logger = LoggerFactory.getLogger(AssetClient.class);

	private Web3j web3j;

	private Credentials credentials;

	public Web3j getWeb3j() {
		return web3j;
	}

	public void setWeb3j(Web3j web3j) {
		this.web3j = web3j;
	}

	public Credentials getCredentials() {
		return credentials;
	}

	public void setCredentials(Credentials credentials) {
		this.credentials = credentials;
	}

	public void recordAssetAddr(String address) throws FileNotFoundException, IOException {
		Properties prop = new Properties();
		prop.setProperty("address", address);
		final Resource contractResource = new ClassPathResource("contract.properties");
		FileOutputStream fileOutputStream = new FileOutputStream(contractResource.getFile());
		prop.store(fileOutputStream, "contract address");
	}

	public String loadAssetAddr() throws Exception {
		// load Asset contact address from contract.properties
		Properties prop = new Properties();
		final Resource contractResource = new ClassPathResource("contract.properties");
		prop.load(contractResource.getInputStream());

		String contractAddress = prop.getProperty("address");
		if (contractAddress == null || contractAddress.trim().equals("")) {
			throw new Exception(" load Asset contract address failed, please deploy it first. ");
		}
		logger.info(" load Asset address from contract.properties, address is {}", contractAddress);
		return contractAddress;
	}

	public void initialize() throws Exception {

		// init the Service
		@SuppressWarnings("resource")
		ApplicationContext context = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
		Service service = context.getBean(Service.class);
		service.run();

		ChannelEthereumService channelEthereumService = new ChannelEthereumService();
		channelEthereumService.setChannelService(service);
		Web3j web3j = Web3j.build(channelEthereumService, 1);

		// init Credentials
		Credentials credentials = Credentials.create(Keys.createEcKeyPair());

		// 设置证书与web3j
		setCredentials(credentials);
		setWeb3j(web3j);

		logger.debug(" web3j is " + web3j + " ,credentials is " + credentials);
	}

	private static BigInteger gasPrice = new BigInteger("30000000");
	private static BigInteger gasLimit = new BigInteger("30000000");

	// @brief Deploy
	// 部署合约
	public void deployAssetAndRecordAddr() {

		try {
			// 部署自己的合约
			// load()加载合约, 需要知道合约地址
			String buy_address = "0xdbaf5d709a0d3545a519d09d467af5f914eba085";
			BuySomething buysth = BuySomething.load(buy_address, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));

			//BuySomething buysth = BuySomething.deploy(web3j, credentials, new StaticGasProvider(gasPrice, gasLimit)).send();

			String address = "0x62261763bc66e58939c65c0113619f8ed4f49240";
			Asset asset = Asset.load(address, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));

			System.out.println(" deploy BuySomething success, contract address is " + buysth.getContractAddress());
		
			System.out.println(" deploy Asset success, contract address is " + asset.getContractAddress());

			recordAssetAddr(asset.getContractAddress());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			System.out.println(" deploy Asset contract failed, error message is  " + e.getMessage());
		}
	}

	public void queryAssetAmount(String assetAccount) {
		try {
			String contractAddress = loadAssetAddr();

			Asset asset = Asset.load(contractAddress, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
			Tuple2<BigInteger, BigInteger> result = asset.select(assetAccount).send();
			if (result.getValue1().compareTo(new BigInteger("0")) == 0) {
				System.out.printf(" asset account %s, value %s \n", assetAccount, result.getValue2());
			} else {
				System.out.printf(" %s asset account is not exist \n", assetAccount);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			logger.error(" queryAssetAmount exception, error message is {}", e.getMessage());

			System.out.printf(" query asset account failed, error message is %s\n", e.getMessage());
		}
	}

	// @brief 功能一 --> 查询 Select
	// select the data in table 
	public List<String> Select(String name) {
		List<String> str = new ArrayList<String>();
		try {
			String buy_address = "0xdbaf5d709a0d3545a519d09d467af5f914eba085";
			BuySomething buysth = BuySomething.load(buy_address, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));

			Tuple3<List<byte[]>, List<BigInteger>, List<byte[]>> result = buysth.select(name).send();

			System.out.println("<---------------- select ---------------->");
			int len = result.getValue1().size();
			for(int i = 0; i < len; ++i){
				String first_name = new String(result.getValue1().get(i));
				System.out.println(first_name);
				String tmp = new String(first_name);
				tmp += "$";

				System.out.println(result.getValue2().get(i));
				tmp += result.getValue2().get(i).toString();
				tmp += "$";

				String second_name = new String(result.getValue3().get(i));
				System.out.println(second_name);
				tmp += second_name;
				str.add(tmp);
			}
			System.out.println("<---------------- select ---------------->");
			
		}catch(Exception e){
			logger.error(" queryAssetAmount exception, error message is {}", e.getMessage());

			System.out.printf(" query asset account failed, error message is %s\n", e.getMessage());
		}
		return str;
	}

	public void registerAssetAccount(String assetAccount, BigInteger amount) {
		try {
			String contractAddress = loadAssetAddr();

			Asset asset = Asset.load(contractAddress, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
			TransactionReceipt receipt = asset.register(assetAccount, amount).send();
			List<RegisterEventEventResponse> response = asset.getRegisterEventEvents(receipt);
			if (!response.isEmpty()) {
				if (response.get(0).ret.compareTo(new BigInteger("0")) == 0) {
					System.out.printf(" register asset account success => asset: %s, value: %s \n", assetAccount,
							amount);
				} else {
					System.out.printf(" register asset account failed, ret code is %s \n",
							response.get(0).ret.toString());
				}
			} else {
				System.out.println(" event log not found, maybe transaction not exec. ");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();

			logger.error(" registerAssetAccount exception, error message is {}", e.getMessage());
			System.out.printf(" register asset account failed, error message is %s\n", e.getMessage());
		}
	}

	// @brief 功能二 --> 购买 Buy
	// 向链端插入新表项
	public void Buy(String name, BigInteger amount, String acceptName) {
		try {
			String buy_address = "0xdbaf5d709a0d3545a519d09d467af5f914eba085";
			BuySomething buysth = BuySomething.load(buy_address, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));

			System.out.println("<---------------- Buy ---------------->");
			TransactionReceipt receipt = buysth.buy(name, amount, acceptName).send();
			System.out.println("<---------------- Buy ---------------->");
			
			/*List<RegisterEventEventResponse> response = asset.getRegisterEventEvents(receipt);
			if (!response.isEmpty()) {
				if (response.get(0).ret.compareTo(new BigInteger("0")) == 0) {
					System.out.printf(" register asset account success => asset: %s, value: %s \n", assetAccount,
							amount);
				} else {
					System.out.printf(" register asset account failed, ret code is %s \n",
							response.get(0).ret.toString());
				}
			} else {
				System.out.println(" event log not found, maybe transaction not exec. ");
			}*/
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();

			logger.error(" registerAssetAccount exception, error message is {}", e.getMessage());
			System.out.printf(" register asset account failed, error message is %s\n", e.getMessage());
		}
	}

	// @brief 功能三 --> 用欠款购买 BuyWithLoan
	// 转义部分欠款来购买东西
	public void BuyWithLoan(String name, BigInteger amount, String acceptName, String oweName, BigInteger partMoney) {
		try {
			String buy_address = "0xdbaf5d709a0d3545a519d09d467af5f914eba085";
			BuySomething buysth = BuySomething.load(buy_address, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));

			System.out.println("<---------------- BuyWithLoan ---------------->");
			TransactionReceipt receipt = buysth.buyWithLoan(partMoney, amount, acceptName, name, oweName).send();
			System.out.println("<---------------- BuyWithLoan ---------------->");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();

			logger.error(" registerAssetAccount exception, error message is {}", e.getMessage());
			System.out.printf(" register asset account failed, error message is %s\n", e.getMessage());
		}
	}

	// @brief 功能四 --> 小企业申请融资 LoanWithBank
	// 小企业利用大企业的借款向银行申请融资
	public void LoanWithBank(String name, String oweCompanyName, BigInteger amount) {
		try {
			String buy_address = "0xdbaf5d709a0d3545a519d09d467af5f914eba085";
			BuySomething buysth = BuySomething.load(buy_address, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));

			System.out.println("<---------------- LoanWithBank ---------------->");
			TransactionReceipt receipt = buysth.loanWithBank(name, oweCompanyName, amount).send();
			System.out.println("<---------------- LoanWithBank ---------------->");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();

			logger.error(" registerAssetAccount exception, error message is {}", e.getMessage());
			System.out.printf(" register asset account failed, error message is %s\n", e.getMessage());
		}
	}

	// @brief 功能五 --> 还钱 PayMoney
	// name还钱给bossName
	public void PayMoney(String name, BigInteger amount, String bossName) {
		try {
			String buy_address = "0xdbaf5d709a0d3545a519d09d467af5f914eba085";
			BuySomething buysth = BuySomething.load(buy_address, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));

			System.out.println("<---------------- PayMoney ---------------->");
			TransactionReceipt receipt = buysth.payMoney(name, amount, bossName).send();
			System.out.println("<---------------- PayMoney ---------------->");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();

			logger.error(" registerAssetAccount exception, error message is {}", e.getMessage());
			System.out.printf(" register asset account failed, error message is %s\n", e.getMessage());
		}
	}


	public void transferAsset(String fromAssetAccount, String toAssetAccount, BigInteger amount) {
		try {
			String contractAddress = loadAssetAddr();
			Asset asset = Asset.load(contractAddress, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
			TransactionReceipt receipt = asset.transfer(fromAssetAccount, toAssetAccount, amount).send();
			List<TransferEventEventResponse> response = asset.getTransferEventEvents(receipt);
			if (!response.isEmpty()) {
				if (response.get(0).ret.compareTo(new BigInteger("0")) == 0) {
					System.out.printf(" transfer success => from_asset: %s, to_asset: %s, amount: %s \n",
							fromAssetAccount, toAssetAccount, amount);
				} else {
					System.out.printf(" transfer asset account failed, ret code is %s \n",
							response.get(0).ret.toString());
				}
			} else {
				System.out.println(" event log not found, maybe transaction not exec. ");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();

			logger.error(" registerAssetAccount exception, error message is {}", e.getMessage());
			System.out.printf(" register asset account failed, error message is %s\n", e.getMessage());
		}
	}

}
