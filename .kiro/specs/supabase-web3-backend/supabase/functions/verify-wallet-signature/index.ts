// Supabase Edge Function for Web3 Wallet Signature Verification
// This function verifies that a user owns a wallet address by validating their signature

import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { ethers } from "https://esm.sh/ethers@6.7.0"

// CORS headers for cross-origin requests
const corsHeaders = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type',
}

serve(async (req) => {
  // Handle CORS preflight requests
  if (req.method === 'OPTIONS') {
    return new Response('ok', { headers: corsHeaders })
  }

  try {
    // Parse request body
    const { walletAddress, signature, message } = await req.json()

    // Validate required fields
    if (!walletAddress || !signature || !message) {
      return new Response(
        JSON.stringify({ 
          error: "Missing required fields",
          details: "walletAddress, signature, and message are required"
        }),
        { 
          status: 400,
          headers: { ...corsHeaders, 'Content-Type': 'application/json' }
        }
      )
    }

    // Verify the signature using ethers.js
    let recoveredAddress: string
    try {
      recoveredAddress = ethers.verifyMessage(message, signature)
    } catch (error) {
      return new Response(
        JSON.stringify({ 
          error: "Invalid signature format",
          details: error.message
        }),
        { 
          status: 400,
          headers: { ...corsHeaders, 'Content-Type': 'application/json' }
        }
      )
    }

    // Compare recovered address with provided wallet address (case-insensitive)
    if (recoveredAddress.toLowerCase() !== walletAddress.toLowerCase()) {
      return new Response(
        JSON.stringify({ 
          error: "Signature verification failed",
          details: "The signature does not match the provided wallet address"
        }),
        { 
          status: 401,
          headers: { ...corsHeaders, 'Content-Type': 'application/json' }
        }
      )
    }

    // Extract and validate timestamp from message to prevent replay attacks
    // Expected message format: "Sign this message to authenticate with IReader: {timestamp}"
    const timestampMatch = message.match(/: (\d+)$/)
    if (!timestampMatch) {
      return new Response(
        JSON.stringify({ 
          error: "Invalid message format",
          details: "Message must contain a timestamp"
        }),
        { 
          status: 400,
          headers: { ...corsHeaders, 'Content-Type': 'application/json' }
        }
      )
    }

    const messageTimestamp = parseInt(timestampMatch[1])
    const currentTimestamp = Date.now()
    const fiveMinutesInMs = 5 * 60 * 1000

    // Check if message is expired (older than 5 minutes)
    if (currentTimestamp - messageTimestamp > fiveMinutesInMs) {
      return new Response(
        JSON.stringify({ 
          error: "Message expired",
          details: "The signature message is older than 5 minutes. Please request a new signature."
        }),
        { 
          status: 401,
          headers: { ...corsHeaders, 'Content-Type': 'application/json' }
        }
      )
    }

    // Check if message is from the future (clock skew protection)
    if (messageTimestamp > currentTimestamp + 60000) { // Allow 1 minute clock skew
      return new Response(
        JSON.stringify({ 
          error: "Invalid timestamp",
          details: "Message timestamp is in the future"
        }),
        { 
          status: 400,
          headers: { ...corsHeaders, 'Content-Type': 'application/json' }
        }
      )
    }

    // Signature verification successful
    return new Response(
      JSON.stringify({ 
        verified: true,
        walletAddress: recoveredAddress,
        timestamp: currentTimestamp
      }),
      { 
        status: 200,
        headers: { ...corsHeaders, 'Content-Type': 'application/json' }
      }
    )

  } catch (error) {
    // Handle unexpected errors
    console.error('Error in verify-wallet-signature:', error)
    
    return new Response(
      JSON.stringify({ 
        error: "Internal server error",
        details: error.message
      }),
      { 
        status: 500,
        headers: { ...corsHeaders, 'Content-Type': 'application/json' }
      }
    )
  }
})
