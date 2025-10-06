compatibility_rules = {
    'A+': ['A+', 'A-', 'O+', 'O-'],
    'A-': ['A-', 'O-'],
    'B+': ['B+', 'B-', 'O+', 'O-'],
    'B-': ['B-', 'O-'],
    'AB+': ['A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-'], # Universal recipient
    'AB-': ['A-', 'B-', 'AB-', 'O-'],
    'O+': ['O+', 'O-'],
    'O-': ['O-']  # Universal donor can only receive from O-
}

def is_compatible(recipient_type, donor_type):
    """
    Checks if a donor's blood type is compatible with a recipient's.
    
    Args:
        recipient_type (str): The blood type of the person receiving blood.
        donor_type (str): The blood type of the person donating blood.
        
    Returns:
        bool: True if compatible, False otherwise.
    """
    
    compatible_donors = compatibility_rules.get(recipient_type, [])
    
    return donor_type in compatible_donors

