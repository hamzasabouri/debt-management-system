# Logos Documentation

## TGR Logo
- Represents: Trésor Public
- Displayed as: Avatar with "TGR" text
- Color: Primary orange (#FF6B35)

## GDP Logo
- Represents: Gestion de Trésor
- Displayed as: Avatar with "GDP" text
- Color: Secondary dark gray (#2D3748)

## Implementation Notes
These logos are currently implemented as Material UI Avatar components with placeholder text. 
For production use, replace these with actual logo images by:
1. Adding logo image files to this assets folder
2. Updating the LoginPage.jsx component to use <img> tags instead of Avatar components
3. Setting the src attribute to point to the logo files

Example replacement code:
```jsx
// Replace the Avatar component with:
<Box textAlign="center">
  <img 
    src="/src/assets/tgr-logo.png" 
    alt="TGR Logo" 
    style={{ width: 80, height: 80, marginBottom: 8 }}
  />
  <Typography variant="caption" sx={{ color: 'text.secondary' }}>
    Trésor Public
  </Typography>
</Box>
```