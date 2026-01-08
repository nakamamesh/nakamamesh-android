package com.NakamaMesh.android.ui.wallet

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigDecimal
import java.math.BigInteger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isConnected by remember { mutableStateOf(false) }
    var walletAddress by remember { mutableStateOf("Not Connected") }
    var nkmaBalance by remember { mutableStateOf("--") }
    var recipientAddress by remember { mutableStateOf("") }
    var sendAmount by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }

    // NKMA Token Contract on BSC
    val nkmaContract = "0xCEf96C5f3e46ddFfe9c1C073cf907dCEA33F2425" // YOU'LL REPLACE THIS!
    val bscRpc = "https://bsc-dataseed.binance.org/"

    // Function to fetch NKMA balance
    fun fetchBalance(address: String) {
        scope.launch {
            isLoading = true
            statusMessage = "Loading balance..."
            try {
                val balance = withContext(Dispatchers.IO) {
                    val web3j = Web3j.build(HttpService(bscRpc))

                    // ERC20 balanceOf function
                    val function = Function(
                        "balanceOf",
                        listOf(Address(address)),
                        listOf(object : TypeReference<Uint256>() {})
                    )

                    val encodedFunction = FunctionEncoder.encode(function)
                    val response = web3j.ethCall(
                        org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(
                            address,
                            nkmaContract,
                            encodedFunction
                        ),
                        org.web3j.protocol.core.DefaultBlockParameterName.LATEST
                    ).send()

                    val value = response.value
                    if (value != null && value != "0x") {
                        val balanceValue = BigInteger(value.substring(2), 16)
                        val decimals = BigDecimal("1000000000000000000") // 18 decimals
                        BigDecimal(balanceValue).divide(decimals).toPlainString()
                    } else {
                        "0"
                    }
                }

                nkmaBalance = balance
                statusMessage = "Balance loaded!"
            } catch (e: Exception) {
                statusMessage = "Error: ${e.message}"
                nkmaBalance = "Error"
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("💎 NKMA Wallet") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF667EEA),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Connect Wallet Button
            Button(
                onClick = {
                    // Open MetaMask to connect
                    try {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("https://metamask.app.link/dapp/nakamamesh.network")
                        }
                        context.startActivity(intent)

                        // For now, simulate connection (user will paste their address)
                        // In production, you'd use WalletConnect for automatic connection
                        statusMessage = "Opening MetaMask... Please copy your address and paste it here!"
                    } catch (e: Exception) {
                        statusMessage = "Error: MetaMask not found. Please install MetaMask!"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isConnected) Color(0xFF4CAF50) else Color(0xFF2196F3)
                ),
                enabled = !isLoading
            ) {
                Text(
                    text = if (isLoading) "Loading..." else if (isConnected) "✅ Connected" else "🔗 Connect MetaMask",
                    fontSize = 18.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Manual address input (for easy testing)
            if (!isConnected) {
                OutlinedTextField(
                    value = walletAddress,
                    onValueChange = {
                        walletAddress = it
                        if (it.startsWith("0x") && it.length == 42) {
                            isConnected = true
                            fetchBalance(it)
                        }
                    },
                    label = { Text("Paste Your MetaMask Address") },
                    placeholder = { Text("0x...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text(
                    text = "Tip: Open MetaMask → Tap your address to copy → Paste here!",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else {
                // Wallet Address Display
                Text(
                    text = walletAddress,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status Message
            if (statusMessage.isNotEmpty()) {
                Text(
                    text = statusMessage,
                    fontSize = 12.sp,
                    color = if (statusMessage.contains("Error")) Color.Red else Color(0xFF4CAF50),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // NKMA Balance
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE3F2FD)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "NKMA Balance",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )

                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(16.dp),
                            color = Color(0xFF2196F3)
                        )
                    } else {
                        Text(
                            text = "$nkmaBalance NKMA",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2196F3)
                        )
                    }

                    if (isConnected && !isLoading) {
                        TextButton(onClick = { fetchBalance(walletAddress) }) {
                            Text("🔄 Refresh", color = Color(0xFF2196F3))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Send NKMA Section
            Text(
                text = "Send NKMA",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // Recipient Address Input
            OutlinedTextField(
                value = recipientAddress,
                onValueChange = { recipientAddress = it },
                label = { Text("Recipient Address (0x...)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = isConnected && !isLoading,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Amount Input
            OutlinedTextField(
                value = sendAmount,
                onValueChange = { sendAmount = it },
                label = { Text("Amount (NKMA)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = isConnected && !isLoading,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Send Button
            Button(
                onClick = {
                    // Open MetaMask to send transaction
                    try {
                        val amountInWei = BigDecimal(sendAmount)
                            .multiply(BigDecimal("1000000000000000000"))
                            .toBigInteger()

                        // ERC20 transfer function
                        val function = Function(
                            "transfer",
                            listOf(
                                Address(recipientAddress),
                                Uint256(amountInWei)
                            ),
                            emptyList()
                        )

                        val data = FunctionEncoder.encode(function)

                        // Create MetaMask deep link for transaction
                        val metamaskUrl = "https://metamask.app.link/send/$nkmaContract@56/transfer?address=$recipientAddress&uint256=$amountInWei"

                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            this.data = Uri.parse(metamaskUrl)
                        }
                        context.startActivity(intent)

                        statusMessage = "Opening MetaMask to confirm transaction..."
                    } catch (e: Exception) {
                        statusMessage = "Error: ${e.message}"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = isConnected &&
                        recipientAddress.isNotEmpty() &&
                        sendAmount.isNotEmpty() &&
                        !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9800)
                )
            ) {
                Text(
                    text = "📤 Send NKMA",
                    fontSize = 18.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Info text
            if (!isConnected) {
                Text(
                    text = "Connect your MetaMask wallet to see balance and send NKMA",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}