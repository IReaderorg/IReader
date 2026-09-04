// Supabase Edge Function for Discord Webhook Proxy
// Securely dispatches webhook messages to Discord channels without exposing webhook tokens in client apps.

import { serve } from "https://deno.land/std@0.168.0/http/server.ts"

const corsHeaders = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type',
}

interface WebhookPayload {
  type: 'crash_report' | 'quote' | 'character_art' | 'share' | 'message'
  content?: string
  username?: string
  title?: string
  description?: string
  fields?: Array<{ name: string; value: string; inline?: boolean }>
  color?: number
  fileBase64?: string
  fileName?: string
  data?: Record<string, unknown>
}

serve(async (req) => {
  if (req.method === 'OPTIONS') {
    return new Response('ok', { headers: corsHeaders })
  }

  try {
    const payload: WebhookPayload = await req.json()

    if (!payload.type) {
      return new Response(
        JSON.stringify({ error: "Missing required 'type' field" }),
        { status: 400, headers: { ...corsHeaders, 'Content-Type': 'application/json' } }
      )
    }

    // Determine target webhook URL from environment secrets
    let targetWebhookUrl: string | undefined

    switch (payload.type) {
      case 'crash_report':
        targetWebhookUrl = Deno.env.get('DISCORD_CRASH_WEBHOOK_URL')
        break
      case 'character_art':
        targetWebhookUrl = Deno.env.get('DISCORD_CHARACTER_ART_WEBHOOK_URL')
        break
      case 'quote':
        targetWebhookUrl = Deno.env.get('DISCORD_QUOTE_WEBHOOK_URL')
        break
      case 'share':
        targetWebhookUrl = Deno.env.get('DISCORD_SHARE_WEBHOOK_URL') || Deno.env.get('DISCORD_QUOTE_WEBHOOK_URL')
        break
      case 'message':
      default:
        targetWebhookUrl = Deno.env.get('DISCORD_GENERAL_WEBHOOK_URL') || Deno.env.get('DISCORD_QUOTE_WEBHOOK_URL')
        break
    }

    if (!targetWebhookUrl) {
      return new Response(
        JSON.stringify({ error: `No webhook configured for type '${payload.type}'` }),
        { status: 503, headers: { ...corsHeaders, 'Content-Type': 'application/json' } }
      )
    }

    // Build Discord-compatible JSON payload
    let discordBody: BodyInit
    const headers: Record<string, string> = {}

    if (payload.fileBase64) {
      // Multipart upload for images
      const formData = new FormData()
      const binaryData = Uint8Array.from(atob(payload.fileBase64), c => c.charCodeAt(0))
      const mimeType = payload.fileName?.endsWith('.png') ? 'image/png' : 'image/jpeg'
      const blob = new Blob([binaryData], { type: mimeType })
      const fileName = payload.fileName || 'upload.jpg'
      
      formData.append('file', blob, fileName)

      const embeds: unknown[] = []
      if (payload.title || payload.description || (payload.fields && payload.fields.length > 0)) {
        embeds.push({
          title: payload.title?.slice(0, 256),
          description: payload.description?.slice(0, 4096),
          color: payload.color || (payload.type === 'crash_report' ? 0xE74C3C : 0x58B9FF),
          fields: payload.fields?.map(f => ({
            name: f.name.slice(0, 256),
            value: f.value.slice(0, 1024),
            inline: f.inline ?? false
          })),
          image: { url: `attachment://${fileName}` },
          timestamp: new Date().toISOString(),
          footer: {
            text: 'IReader App'
          }
        })
      }

      if (embeds.length > 0) {
        const payloadJson = {
          content: payload.content ? payload.content.slice(0, 2000) : undefined,
          username: payload.username || 'IReader',
          embeds
        }
        formData.append('payload_json', JSON.stringify(payloadJson))
      } else {
        formData.append('content', (payload.content || '').slice(0, 2000))
        formData.append('username', payload.username || 'IReader')
      }

      discordBody = formData
    } else {
      // JSON payload with embeds
      const embeds: unknown[] = []

      if (payload.title || payload.description || (payload.fields && payload.fields.length > 0)) {
        embeds.push({
          title: payload.title?.slice(0, 256),
          description: payload.description?.slice(0, 4096),
          color: payload.color || (payload.type === 'crash_report' ? 0xE74C3C : 0x3498DB),
          fields: payload.fields?.map(f => ({
            name: f.name.slice(0, 256),
            value: f.value.slice(0, 1024),
            inline: f.inline ?? false
          })),
          timestamp: new Date().toISOString(),
          footer: {
            text: 'IReader App'
          }
        })
      }

      discordBody = JSON.stringify({
        content: payload.content ? payload.content.slice(0, 2000) : undefined,
        username: payload.username || (payload.type === 'crash_report' ? 'IReader Crash Reporter' : 'IReader'),
        embeds: embeds.length > 0 ? embeds : undefined
      })
      headers['Content-Type'] = 'application/json'
    }

    // Forward request to Discord
    const discordResponse = await fetch(targetWebhookUrl, {
      method: 'POST',
      headers,
      body: discordBody
    })

    if (!discordResponse.ok) {
      const errorText = await discordResponse.text()
      return new Response(
        JSON.stringify({ error: "Discord rejected webhook", status: discordResponse.status, details: errorText }),
        { status: discordResponse.status, headers: { ...corsHeaders, 'Content-Type': 'application/json' } }
      )
    }

    return new Response(
      JSON.stringify({ success: true, message: "Dispatched to Discord successfully" }),
      { status: 200, headers: { ...corsHeaders, 'Content-Type': 'application/json' } }
    )
  } catch (error) {
    return new Response(
      JSON.stringify({ error: "Internal Server Error", details: error.message }),
      { status: 500, headers: { ...corsHeaders, 'Content-Type': 'application/json' } }
    )
  }
})
