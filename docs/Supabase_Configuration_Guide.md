# Supabase Configuration Guide

This guide explains how to configure IReader to use Supabase backend with Web3 wallet authentication.

## Overview

IReader supports optional Supabase backend integration for:
- Web3 wallet-based authentication (no passwords required)
- Cross-device reading progress synchronization
- Real-time updates across devices

## Prerequisites

1. A Supabase account (free tier available at https://supabase.com)
2. A Supabase project with the required database schema
3. Your Supabase project URL and anon key

## Platform-Specific Configuration

### Android Configuration

1. Open `local.properties` in the project root
2. Add your Supabase credentials:

```properties
supabase.url=https://your-project.supabase.co
supabase.anon.key=your-anon-key-here
```

3. Rebuild the project

**Note:** The `local.properties` file is gitignored and should never be committed to version control.

### Desktop Configuration

#### Option 1: Application Directory (Recommended)

1. Copy `config.properties.example` to `config.properties` in the project root
2. Edit `config.properties` and add your credentials:

```properties
supabase.url=https://your-project.supabase.co
supabase.anon.key=your-anon-key-here
supabase.realtime.enabled=true
supabase.sync.interval.ms=30000
```

#### Option 2: User Home Directory

1. Create a directory: `~/.ireader/`
2. Create a file: `~/.ireader/config.properties`
3. Add your credentials as shown above

**Note:** The application directory config takes precedence over the home directory config.

## Configuration Options

### Required Settings

- `supabase.url`: Your Supabase project URL (e.g., `https://abcdefgh.supabase.co`)
- `supabase.anon.key`: Your Supabase anonymous/public API key

### Optional Settings (Desktop Only)

- `supabase.realtime.enabled`: Enable real-time sync (default: `true`)
- `supabase.sync.interval.ms`: Sync interval in milliseconds (default: `30000` = 30 seconds)

## Security Considerations

### API Keys

- **Anon Key**: Safe to use in client applications. Has limited permissions defined by Row Level Security (RLS) policies.
- **Service Role Key**: NEVER use in client applications. Keep it secret and only use on the server side.

### Session Storage

- **Android**: Uses EncryptedSharedPreferences with AES256-GCM encryption
- **Desktop**: Uses Java Preferences API with AES encryption

Sessions expire after 30 days of inactivity.

## Wallet Address Validation

The application validates Ethereum wallet addresses using the following format:
- Must start with `0x`
- Must contain exactly 40 hexadecimal characters (0-9, a-f, A-F)
- Example: `0xF9Abb8B0e0e8e8e8e8e8e8e8e8e8e8e8e87eCc`

## Disabling Remote Features

To disable remote sync features:

1. **Android**: Remove or comment out the Supabase configuration in `local.properties`
2. **Desktop**: Delete or rename `config.properties`

The application will work normally in local-only mode without any backend connection.

## Troubleshooting

### Configuration Not Loading

**Android:**
- Ensure `local.properties` exists in the project root
- Rebuild the project after adding configuration
- Check that BuildConfig fields are generated correctly

**Desktop:**
- Check that `config.properties` exists in the correct location
- Verify file permissions (must be readable)
- Check application logs for configuration errors

### Authentication Issues

- Verify your Supabase URL and anon key are correct
- Check that your Supabase project is active
- Ensure Row Level Security policies are configured correctly
- Verify the Edge Function for signature verification is deployed

### Sync Not Working

- Check network connectivity
- Verify Supabase Realtime is enabled in your project
- Check that reading progress table exists with correct schema
- Review application logs for sync errors

## Next Steps

After configuration:

1. Set up your Supabase database schema (see `docs/Supabase_Setup_Guide.md`)
2. Deploy the wallet signature verification Edge Function
3. Configure Row Level Security policies
4. Test authentication with a Web3 wallet

## Support

For issues or questions:
- Check the main README.md
- Review Supabase documentation: https://supabase.com/docs
- Open an issue on GitHub
