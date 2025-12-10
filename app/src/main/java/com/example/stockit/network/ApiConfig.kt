package com.example.stockit.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.io.IOException
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import javax.net.ssl.HostnameVerifier

// Custom retry interceptor
class RetryInterceptor(private val maxRetries: Int = 3) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        var response: Response? = null
        var exception: IOException? = null
        
        for (attempt in 0 until maxRetries) {
            try {
                response?.close()
                response = chain.proceed(request)
                
                if (response.isSuccessful) {
                    return response
                }
                
                if (response.code < 500) {
                    return response
                }
                
            } catch (e: IOException) {
                exception = e
                if (attempt == maxRetries - 1) {
                    throw e
                }
            }
            
            if (attempt < maxRetries - 1) {
                Thread.sleep((1000 * (attempt + 1)).toLong())
            }
        }
        
        return response ?: throw (exception ?: IOException("Max retries exceeded"))
    }
}

object ApiConfig {
    // Production backend server on Render
    // Permanent URL - no need to restart or update!
    private const val BASE_URL = "https://stockit-api-yqi5.onrender.com/"
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val retryInterceptor = RetryInterceptor(maxRetries = 3)
    
    // Create a trust manager that accepts all certificates
    private fun getTrustAllCerts(): Array<TrustManager> {
        return arrayOf(object : X509TrustManager {
            @Throws(CertificateException::class)
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
                // Accept all client certificates
            }
            
            @Throws(CertificateException::class)
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
                // Accept all server certificates
            }
            
            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }
        })
    }
    
    // Create SSL socket factory with TLS protocol
    private fun getSSLSocketFactory(): SSLSocketFactory {
        return try {
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, getTrustAllCerts(), SecureRandom())
            sslContext.socketFactory
        } catch (e: Exception) {
            android.util.Log.e("ApiConfig", "Failed to create SSL socket factory", e)
            throw RuntimeException("Failed to create SSL socket factory", e)
        }
    }
    
    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(retryInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .sslSocketFactory(getSSLSocketFactory(), getTrustAllCerts()[0] as X509TrustManager)
        .hostnameVerifier(HostnameVerifier { hostname, session -> 
            // Accept all hostnames - FOR DEVELOPMENT ONLY
            android.util.Log.d("ApiConfig", "Accepting hostname: $hostname")
            true 
        })
        .build()
    
    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val stockApiService: StockApiService = retrofit.create(StockApiService::class.java)
    val authApiService: AuthApiService = retrofit.create(AuthApiService::class.java)
}

// Stock API Service Interface
interface StockApiService {
    // Basic stock data
    @GET("api/stock/{symbol}")
    suspend fun getStock(@Path("symbol") symbol: String, @Query("cache") cache: String = "true"): StockResponse
    
    @GET("api/stock/{symbol}/quote")
    suspend fun getStockQuote(@Path("symbol") symbol: String, @Query("cache") cache: String = "true"): StockResponse
    
    @GET("api/stock/{symbol}/details")
    suspend fun getStockDetails(@Path("symbol") symbol: String, @Query("cache") cache: String = "true"): StockResponse
    
    // Historical data endpoints
    @GET("api/stock/{symbol}/intraday")
    suspend fun getIntradayData(@Path("symbol") symbol: String, @Query("cache") cache: String = "true"): ChartDataResponse
    
    @GET("api/stock/{symbol}/weekly")
    suspend fun getWeeklyData(@Path("symbol") symbol: String, @Query("cache") cache: String = "true"): ChartDataResponse
    
    @GET("api/stock/{symbol}/monthly")
    suspend fun getMonthlyData(@Path("symbol") symbol: String, @Query("cache") cache: String = "true"): ChartDataResponse
    
    @GET("api/stock/{symbol}/quarterly")
    suspend fun getQuarterlyData(@Path("symbol") symbol: String, @Query("cache") cache: String = "true"): ChartDataResponse
    
    @GET("api/stock/{symbol}/yearly")
    suspend fun getYearlyData(@Path("symbol") symbol: String, @Query("cache") cache: String = "true"): ChartDataResponse
    
    @GET("api/stock/{symbol}/historical")
    suspend fun getHistoricalData(
        @Path("symbol") symbol: String,
        @Query("period") period: String = "1mo",
        @Query("interval") interval: String = "1d",
        @Query("cache") cache: String = "true"
    ): ChartDataResponse
    
    // Search endpoints
    @GET("api/search")
    suspend fun searchStocks(
        @Query("q") query: String,
        @Query("limit") limit: Int = 10,
        @Query("online") online: String = "true"
    ): StockSearchResponse
    
    @GET("api/search/online")
    suspend fun searchOnlineStocks(
        @Query("q") query: String,
        @Query("limit") limit: Int = 10
    ): StockSearchResponse
    
    @GET("api/search-quote")
    suspend fun searchAndQuote(
        @Query("q") query: String,
        @Query("cache") cache: String = "true"
    ): StockResponse
    
    // Trending stocks (replaces popular)
    @GET("api/trending")
    suspend fun getTrendingStocks(
        @Query("limit") limit: Int = 30,
        @Query("update") update: String = "false",
        @Query("force") force: String = "false"
    ): TrendingStocksResponse
    
    @POST("api/trending/update")
    suspend fun updateTrendingStocks(
        @Header("Authorization") token: String
    ): TrendingStocksResponse
    
    @GET("api/trending/scrape")
    suspend fun scrapeTrendingStocks(
        @Header("Authorization") token: String
    ): TrendingStocksResponse
    
    // Keep popular for backward compatibility (but mark as deprecated)
    @Deprecated("Use getTrendingStocks instead")
    @GET("api/popular")
    suspend fun getPopularStocks(
        @Query("category") category: String = "nifty50",
        @Query("limit") limit: Int = 10
    ): PopularStocksResponse
    
    // Compare stocks
    @POST("api/stocks/compare")
    suspend fun compareStocks(@Body request: CompareStocksRequest): StockCompareResponse
    
    // User trading endpoints (require authentication)
    @GET("api/user/wallet")
    suspend fun getUserWallet(@Header("Authorization") token: String): WalletResponse
    
    @POST("api/user/wallet/deposit")
    suspend fun depositToWallet(
        @Header("Authorization") token: String,
        @Body request: WalletTransactionRequest
    ): WalletResponse
    
    @POST("api/user/wallet/withdraw")
    suspend fun withdrawFromWallet(
        @Header("Authorization") token: String,
        @Body request: WalletTransactionRequest
    ): WalletResponse
    
    @GET("api/user/wallet/transactions")
    suspend fun getWalletTransactions(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): WalletTransactionsResponse
    
    @POST("api/user/stocks/buy")
    suspend fun buyStock(
        @Header("Authorization") token: String,
        @Body request: TradeRequest
    ): TradeResponse
    
    @POST("api/user/stocks/sell")
    suspend fun sellStock(
        @Header("Authorization") token: String,
        @Body request: TradeRequest
    ): TradeResponse
    
    @GET("api/user/portfolio")
    suspend fun getUserPortfolio(
        @Header("Authorization") token: String,
        @Query("updatePrices") updatePrices: String = "true"
    ): PortfolioResponse
    
    @GET("api/user/transactions")
    suspend fun getTransactionHistory(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): TransactionHistoryResponse
    
    @GET("api/user/summary")
    suspend fun getUserSummary(@Header("Authorization") token: String): UserSummaryResponse
    
    @POST("api/user/stocks/check-affordability")
    suspend fun checkAffordability(
        @Header("Authorization") token: String,
        @Body request: AffordabilityRequest
    ): AffordabilityResponse

    // Watchlist endpoints
    @GET("api/user/watchlist")
    suspend fun getUserWatchlist(
        @Header("Authorization") token: String,
        @Query("prices") prices: String = "true"
    ): WatchlistResponse

    @POST("api/user/watchlist")
    suspend fun addToWatchlist(
        @Header("Authorization") token: String,
        @Body request: AddToWatchlistRequest
    ): WatchlistActionResponse

    @DELETE("api/user/watchlist/{symbol}")
    suspend fun removeFromWatchlist(
        @Header("Authorization") token: String,
        @Path("symbol") symbol: String
    ): WatchlistActionResponse

    @GET("api/user/watchlist/check/{symbol}")
    suspend fun checkWatchlistStatus(
        @Header("Authorization") token: String,
        @Path("symbol") symbol: String
    ): WatchlistActionResponse

    // Add the missing holdings endpoint
    @GET("api/user/holdings/{symbol}")
    suspend fun getUserStockHolding(
        @Header("Authorization") token: String,
        @Path("symbol") symbol: String
    ): UserHoldingResponse
}

// Auth API Service Interface
interface AuthApiService {
    @POST("api/auth/signin-simple")
    suspend fun signIn(@Body request: SignInRequest): AuthResponse
    
    @POST("api/auth/signup-simple")
    suspend fun signUp(@Body request: SignUpRequest): AuthResponse
}

// Request Data Classes
data class SignInRequest(
    val email: String,
    val password: String
)

data class SignUpRequest(
    val fullName: String,
    val email: String,
    val password: String,
    val confirmPassword: String
)

data class CompareStocksRequest(
    val symbols: List<String>,
    val cache: Boolean = true
)

data class WalletTransactionRequest(
    val amount: Double,
    val description: String? = null
)

data class TradeRequest(
    val symbol: String,
    val quantity: Int,
    val pricePerShare: Double? = null
)

data class AffordabilityRequest(
    val symbol: String,
    val quantity: Int,
    val pricePerShare: Double? = null
)

// Response Data Classes
data class AuthResponse(
    val success: Boolean,
    val message: String? = null,
    val access_token: String? = null,
    val refresh_token: String? = null,
    val user_id: String? = null,
    val user_email: String? = null,
    val user_fullName: String? = null,
    val starting_balance: Double? = null,
    val timestamp: String? = null
)

data class StockResponse(
    val success: Boolean,
    val data: Any? = null,
    val error: String? = null,
    val timestamp: String? = null
)

data class StockSearchResponse(
    val success: Boolean,
    val data: Any? = null,
    val timestamp: String? = null
)

data class PopularStocksResponse(
    val success: Boolean,
    val data: Any? = null,
    val timestamp: String? = null
)

data class StockCompareResponse(
    val success: Boolean,
    val data: Any? = null,
    val symbolsCompared: Int? = null,
    val timestamp: String? = null
)

data class WalletResponse(
    val success: Boolean,
    val userId: Int? = null,
    val balance: Double? = null,
    val totalInvested: Double? = null,
    val totalCurrentValue: Double? = null,
    val totalProfitLoss: Double? = null,
    val totalProfitLossPercent: Double? = null,
    val totalNetWorth: Double? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class PortfolioResponse(
    val success: Boolean,
    val totalHoldings: Int? = null,
    val totalInvested: Double? = null,
    val totalCurrentValue: Double? = null,
    val totalPnL: Double? = null,
    val totalPnLPercent: Double? = null,
    val holdings: List<PortfolioHolding>? = null,
    val timestamp: String? = null,
    val error: String? = null
)

// Remove the old PortfolioData class as it's not needed
// The PortfolioHolding class should match the actual API response:
data class PortfolioHolding(
    val symbol: String,
    val companyName: String,
    val quantity: Int,
    val avgPrice: Double,
    val currentPrice: Double,
    val investedAmount: Double,
    val currentValue: Double,
    val pnl: Double,
    val pnlPercent: Double,
    val firstBuyDate: String,
    val lastUpdated: String
)

data class TransactionHistoryResponse(
    val success: Boolean,
    val totalTransactions: Int? = null,
    val limit: Int? = null,
    val offset: Int? = null,
    val transactions: List<TransactionHistoryItem>? = null,
    val timestamp: String? = null,
    val error: String? = null
)

data class TransactionHistoryItem(
    val id: Int,
    val symbol: String,
    val companyName: String,
    val transactionType: String,
    val quantity: Int,
    val price: Double,
    val totalAmount: Double,
    val transactionDate: String
)

data class UserSummaryResponse(
    val success: Boolean,
    val userId: Int? = null,
    val balance: Double? = null,
    val totalInvested: Double? = null,
    val totalCurrentValue: Double? = null,
    val totalProfitLoss: Double? = null,
    val totalProfitLossPercent: Double? = null,
    val totalNetWorth: Double? = null,
    val totalHoldings: Int? = null,
    val portfolioInvested: Double? = null,
    val portfolioCurrentValue: Double? = null,
    val portfolioPnL: Double? = null,
    val portfolioPnLPercent: Double? = null,
    val holdings: List<Any>? = null,
    val recentStockTransactions: List<Any>? = null,
    val recentWalletTransactions: List<Any>? = null,
    val walletUpdatedAt: String? = null,
    val data: Any? = null,
    val timestamp: String? = null
)

data class UserHoldingResponse(
    val success: Boolean,
    val symbol: String?,
    val owns: Boolean?,
    val quantity: Int?,
    val averagePrice: Double?,
    val investedAmount: Double?,
    val currentPrice: Double?,
    val currentValue: Double?,
    val profitLoss: Double?,
    val firstBuyDate: String?,
    val message: String?,
    val error: String?,
    val timestamp: String?
)

// Updated response class for trending stocks
data class TrendingStocksResponse(
    val success: Boolean,
    val count: Int?,
    val source: String?,
    val lastUpdated: String?,
    val cached: Boolean?,
    val stocks: List<TrendingStock>?,
    val error: String?,
    val timestamp: String?
)

data class TrendingStock(
    val symbol: String,
    val name: String?,
    val price: Double?,
    val change: Double?,
    val changePercent: Double?,
    val volume: Long?,
    val high: Double?,
    val low: Double?,
    val rank: Int?,
    val positive: Boolean?
)

// Add specific response classes for chart data
data class ChartDataResponse(
    val success: Boolean,
    val symbol: String? = null,
    val period: String? = null,
    val interval: String? = null,
    val count: Int? = null,
    val prices: List<PricePoint>? = null,
    val meta: ChartMeta? = null,
    val error: String? = null,
    val timestamp: String? = null
)

data class PricePoint(
    val time: String,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long
)

data class ChartMeta(
    val currency: String? = null,
    val exchange: String? = null,
    val cached: Boolean? = null
)

data class AffordabilityResponse(
    val success: Boolean,
    val data: Any? = null,
    val error: String? = null,
    val timestamp: String? = null
)

data class WatchlistResponse(
    val success: Boolean,
    val totalStocks: Int?,
    val stocks: List<WatchlistStock>?,
    val error: String?,
    val timestamp: String?
)

data class WatchlistActionResponse(
    val success: Boolean,
    val added: Boolean?,
    val removed: Boolean?,
    val symbol: String?,
    val companyName: String?,
    val message: String?,
    val error: String?,
    val inWatchlist: Boolean?,
    val timestamp: String?
)

data class WatchlistStock(
    val symbol: String,
    val companyName: String?,
    val price: Double?,
    val change: Double?,
    val changePercent: Double?,
    val high: Double?,
    val low: Double?,
    val volume: Long?,
    val addedAt: String?,
    val inWatchlist: Boolean = true
)

data class AddToWatchlistRequest(
    val symbol: String
)

data class WalletTransactionsResponse(
    val success: Boolean,
    val data: WalletTransactionsData? = null,
    val error: String? = null,
    val timestamp: String? = null
)

data class WalletTransactionsData(
    val totalTransactions: Int,
    val limit: Int,
    val offset: Int,
    val transactions: List<WalletTransactionItem>,
    val timestamp: String
)

data class WalletTransactionItem(
    val id: Int,
    val type: String, // "DEPOSIT" or "WITHDRAW"
    val amount: Double,
    val description: String?,
    val createdAt: String
)

data class TradeResponse(
    val success: Boolean,
    val message: String? = null,
    val error: String? = null,
    val data: TradeData? = null,
    val timestamp: String? = null
)

data class TradeData(
    val transactionId: Int? = null,
    val symbol: String? = null,
    val quantity: Int? = null,
    val price: Double? = null,
    val totalAmount: Double? = null,
    val type: String? = null, // "BUY" or "SELL"
    val timestamp: String? = null
)